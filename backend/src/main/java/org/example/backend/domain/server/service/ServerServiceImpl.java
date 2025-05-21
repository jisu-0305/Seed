package org.example.backend.domain.server.service;

import com.jcraft.jsch.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.request.server.HttpsConvertRequest;
import org.example.backend.domain.fcm.service.NotificationServiceImpl;
import org.example.backend.domain.fcm.template.NotificationMessageTemplate;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.domain.jenkins.entity.JenkinsInfo;
import org.example.backend.domain.jenkins.repository.JenkinsInfoRepository;
import org.example.backend.domain.project.entity.*;
import org.example.backend.domain.project.enums.ServerStatus;
import org.example.backend.domain.project.enums.FileType;
import org.example.backend.domain.project.repository.*;
import org.example.backend.domain.server.entity.HttpsLog;
import org.example.backend.domain.server.repository.HttpsLogRepository;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerServiceImpl implements ServerService {

    private final ServerStatusService serverStatusService;
    private final UserRepository userRepository;
    private final UserProjectRepository userProjectRepository;
    private final ProjectRepository projectRepository;
    private final RedisSessionManager redisSessionManager;
    private final GitlabService gitlabService;
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final HttpsLogRepository httpsLogRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationServiceImpl notificationService;

    private static final String NGINX_CONF_PATH = "/etc/nginx/sites-available/app.conf";
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectFileRepository projectFileRepository;
    private final ApplicationEnvVariableListRepository applicationEnvVariableListRepository;

    @Override
    public void registerDeployment(Long projectId, MultipartFile pemFile, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        byte[] frontEnv = projectFileRepository.findByProjectIdAndFileType(projectId, FileType.FRONTEND_ENV)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRONT_ENV_NOT_FOUND)).getData();

        byte[] backEnv = projectFileRepository.findByProjectIdAndFileType(projectId, FileType.BACKEND_ENV)
                .orElseThrow(() -> new BusinessException(ErrorCode.BACK_ENV_NOT_FOUND)).getData();

        String host = project.getServerIP();
        Session sshSession = null;

        try {
            // 1) 원격 서버 세션 등록
            log.info("세션 생성 시작");
            sshSession = createSessionWithPem(pemFile.getBytes(), host);
            log.info("SSH 연결 성공: {}", host);

            // 2) 스크립트 실행
            autoDeploymentSettingProcess(sshSession, user, project, frontEnv, backEnv);

            // 3) 프로젝트 자동 배포 활성화
            serverStatusService.updateStatus(project, ServerStatus.FINISH);

            // 4) Jenkins API 토큰 발급 및 스크립트 정리
            log.info("Jenkins API 토큰 발급 시작");
            issueAndSaveToken(projectId, host, sshSession);
            execCommand(sshSession, "sudo rm -f /var/lib/jenkins/init.groovy.d/init_token.groovy");
            execCommand(sshSession, "sudo rm -f /tmp/jenkins_token");
            log.info("Jenkins 토큰 발급 및 스크립트 정리 완료");

            // 5) 세팅 성공 메시지 전송
            notificationService.notifyProjectStatusForUsers(
                    projectId,
                    NotificationMessageTemplate.EC2_SETUP_COMPLETED_SUCCESS
            );

        } catch (Exception e) {
            log.error("배포 중 오류 발생: {}", e.getMessage(), e);
            serverStatusService.updateStatus(project, ServerStatus.FAIL);

            notificationService.notifyProjectStatusForUsers(
                    projectId,
                    NotificationMessageTemplate.EC2_SETUP_FAILED
            );

            throw new BusinessException(ErrorCode.AUTO_DEPLOYMENT_SETTING_FAILED);
        } finally {
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
            }
        }
    }

    public void autoDeploymentSettingProcess(Session sshSession, User user, Project project, byte[] frontEnvFile, byte[] backEnvFile) throws JSchException, IOException {
        String url = project.getRepositoryUrl();
        String repositoryUrl = url.substring(0, url.length() - 4);

        GitlabProject gitlabProject = gitlabService.getProjectByUrl(user.getGitlabPersonalAccessToken(), repositoryUrl);
        String projectPath = "/var/lib/jenkins/jobs/auto-created-deployment-job/" + gitlabProject.getName();
        String gitlabProjectUrlWithToken = "https://" + user.getUserIdentifyId() + ":" + user.getGitlabPersonalAccessToken() + "@lab.ssafy.com/" + gitlabProject.getPathWithNamespace() + ".git";

        setSwapMemory(sshSession, project);
        updatePackageManager(sshSession, project);
        installJDK(sshSession, project);
        installDocker(sshSession, project);
        runApplicationList(sshSession, project, backEnvFile);
        installNginx(sshSession, project, project.getServerIP());
        installJenkins(sshSession, project);
        installJenkinsPlugins(sshSession, project);
        setJenkinsConfiguration(sshSession, project, user.getUserIdentifyId(), user.getGitlabPersonalAccessToken(), frontEnvFile, backEnvFile);
        createJenkinsPipeline(sshSession, project, "auto-created-deployment-job", project.getRepositoryUrl(), "gitlab-token", project.getGitlabTargetBranchName());
        createJenkinsFile(sshSession, gitlabProjectUrlWithToken, projectPath, gitlabProject.getName(), project.getGitlabTargetBranchName(), gitlabProject.getPathWithNamespace(), project);
        createDockerfileForFrontend(sshSession, projectPath, project.getGitlabTargetBranchName() ,project);
        createGitlabWebhook(sshSession, project, user.getGitlabPersonalAccessToken(), gitlabProject.getGitlabProjectId(), "auto-created-deployment-job", project.getServerIP(), project.getGitlabTargetBranchName());
        createDockerfileForBackend(sshSession, projectPath, project.getGitlabTargetBranchName(), project);
    }

    /**
     * 실시간 출력을 모니터링하면서 명령을 실행하는 메서드
     */
//    private String execCommandWithLiveOutput(Session session, String command, long timeoutMs) throws JSchException, IOException {
//        ChannelExec channel = null;
//
//        try {
//            channel = (ChannelExec) session.openChannel("exec");
//            channel.setCommand(command);
//
//            // 표준 출력 스트림 설정
//            InputStream stdout = channel.getInputStream();
//            InputStream stderr = channel.getErrStream();
//
//            StringBuilder outputBuilder = new StringBuilder();
//
//            channel.connect(30 * 1000);
//
//            byte[] buffer = new byte[1024];
//            long startTime = System.currentTimeMillis();
//            long lastOutputTime = startTime;
//
//            while (true) {
//                // 타임아웃 체크
//                if (System.currentTimeMillis() - startTime > timeoutMs) {
//                    log.error("명령 타임아웃: {}", command);
//                    throw new IOException("명령 실행 타임아웃: " + command);
//                }
//
//                // 지정된 시간 동안 출력이 없으면 프로세스 확인
//                if (System.currentTimeMillis() - lastOutputTime > 15 * 60 * 1000) {
//                    log.warn("명령 실행 중 5분 동안 출력 없음, 프로세스 상태 확인: {}", command);
//                    // 관련 프로세스 확인
//                    ChannelExec checkChannel = (ChannelExec) session.openChannel("exec");
//                    checkChannel.setCommand("ps aux | grep -E 'apt|dpkg|jenkins' | grep -v grep");
//                    ByteArrayOutputStream checkOutput = new ByteArrayOutputStream();
//                    checkChannel.setOutputStream(checkOutput);
//                    checkChannel.connect();
//
//                    try {
//                        while (!checkChannel.isClosed()) {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                Thread.currentThread().interrupt();
//                            }
//                        }
//                    } finally {
//                        checkChannel.disconnect();
//                    }
//
//                    log.info("관련 프로세스 상태:\n{}", checkOutput.toString());
//                    lastOutputTime = System.currentTimeMillis();  // 리셋
//                }
//
//                // 출력 읽기
//                while (stdout.available() > 0) {
//                    int i = stdout.read(buffer, 0, buffer.length);
//                    if (i < 0) break;
//                    String output = new String(buffer, 0, i, StandardCharsets.UTF_8);
//                    outputBuilder.append(output);
//                    log.debug("명령 출력: {}", output);
//                    lastOutputTime = System.currentTimeMillis();
//                }
//
//                // 오류 출력 읽기
//                while (stderr.available() > 0) {
//                    int i = stderr.read(buffer, 0, buffer.length);
//                    if (i < 0) break;
//                    String error = new String(buffer, 0, i, StandardCharsets.UTF_8);
//                    outputBuilder.append("[ERROR] ").append(error);
//                    log.warn("명령 오류 출력: {}", error);
//                    lastOutputTime = System.currentTimeMillis();
//                }
//
//                // 명령 완료 확인
//                if (channel.isClosed()) {
//                    int exitStatus = channel.getExitStatus();
//
//                    // 마지막 출력 확인
//                    while (stdout.available() > 0) {
//                        int i = stdout.read(buffer, 0, buffer.length);
//                        if (i < 0) break;
//                        String output = new String(buffer, 0, i, StandardCharsets.UTF_8);
//                        outputBuilder.append(output);
//                    }
//
//                    while (stderr.available() > 0) {
//                        int i = stderr.read(buffer, 0, buffer.length);
//                        if (i < 0) break;
//                        String error = new String(buffer, 0, i, StandardCharsets.UTF_8);
//                        outputBuilder.append("[ERROR] ").append(error);
//                    }
//
//                    if (exitStatus != 0 && !command.contains("|| true")) {
//                        String errorMsg = "명령 실패 (exit=" + exitStatus + "): " + command + "\n" + outputBuilder.toString();
//                        log.error(errorMsg);
//                        throw new IOException(errorMsg);
//                    }
//
//                    break;
//                }
//
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    throw new IOException("명령 대기 중 인터럽트", e);
//                }
//            }
//
//            return outputBuilder.toString();
//        } finally {
//            if (channel != null && channel.isConnected()) {
//                channel.disconnect();
//            }
//        }
//    }

    private String execCommandWithLiveOutput(Session session, String command, long timeoutMs) throws JSchException, IOException {
        ChannelExec channel = null;
        ExecutorService executor = null;
        AtomicBoolean commandCompleted = new AtomicBoolean(false);

        try {
            // 1. 채널 설정
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // 2. 표준 출력 스트림 설정
            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();
            StringBuilder outputBuilder = new StringBuilder();

            // 3. 세션 모니터링을 위한 워치독 스레드 생성
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                t.setName("SSH-WatchDog-" + command.hashCode());
                return t;
            });

            final ChannelExec finalChannel = channel;
            Future<String> watchdog = executor.submit(() -> {
                log.debug("워치독 스레드 시작: {}", command);
                long startTime = System.currentTimeMillis();
                long lastActivityTime = startTime;
                long lastHeartbeatTime = startTime;

                try {
                    while (!commandCompleted.get()) {
                        long currentTime = System.currentTimeMillis();

                        // 3.1. 전체 타임아웃 체크
                        if (currentTime - startTime > timeoutMs) {
                            log.error("명령 타임아웃 ({}ms): {}", timeoutMs, command);
                            finalChannel.disconnect();
                            return "TIMEOUT";
                        }

                        // 3.2. 세션 상태 주기적 확인 (15초마다)
                        if (currentTime - lastHeartbeatTime > 15 * 1000) {
                            try {
                                if (!session.isConnected()) {
                                    log.error("SSH 세션이 끊어졌습니다. 명령: {}", command);
                                    return "SESSION_DISCONNECTED";
                                }

                                if (finalChannel.isClosed()) {
                                    int exitStatus = finalChannel.getExitStatus();
                                    log.debug("채널이 닫혔습니다. 종료 상태: {}", exitStatus);
                                    return exitStatus == 0 ? "COMPLETED" : "FAILED";
                                }

                                lastHeartbeatTime = currentTime;
                            } catch (Exception e) {
                                log.error("세션 상태 확인 중 오류: {}", e.getMessage());
                                return "SESSION_ERROR";
                            }
                        }

                        // 3.3. 명령 실행 중 무응답 상태 확인
                        if (currentTime - lastActivityTime > 60 * 1000) { // 1분간 활동 없음
                            log.debug("1분간 활동 없음 감지, 세션 상태 확인 중: {}", command);

                            // 3.4. 5분 이상 활동 없으면 프로세스 상태 확인
                            if (currentTime - lastActivityTime > 5 * 60 * 1000) {
                                log.warn("명령 실행 중 5분 동안 출력 없음, 프로세스 상태 확인: {}", command);

                                try {
                                    if (session.isConnected()) {
                                        ChannelExec checkChannel = (ChannelExec) session.openChannel("exec");
                                        checkChannel.setCommand("ps aux | grep -E 'apt|dpkg|jenkins' | grep -v grep");
                                        ByteArrayOutputStream checkOutput = new ByteArrayOutputStream();
                                        checkChannel.setOutputStream(checkOutput);
                                        checkChannel.connect(10 * 1000); // 10초 연결 타임아웃

                                        long checkStartTime = System.currentTimeMillis();
                                        while (!checkChannel.isClosed()) {
                                            if (System.currentTimeMillis() - checkStartTime > 20 * 1000) { // 20초 이상 걸리면 중단
                                                checkChannel.disconnect();
                                                log.warn("프로세스 상태 확인 타임아웃");
                                                break;
                                            }
                                            Thread.sleep(100);
                                        }

                                        checkChannel.disconnect();
                                        log.info("관련 프로세스 상태:\n{}", checkOutput.toString());

                                        // 프로세스 확인 후에도 여전히 출력이 없으면 상태를 업데이트하지 않음
                                    } else {
                                        log.error("세션이 끊어진 상태에서 프로세스 확인 시도");
                                        return "SESSION_DISCONNECTED";
                                    }
                                } catch (Exception e) {
                                    log.error("프로세스 상태 확인 중 오류: {}", e.getMessage());
                                }

                                // 3.5. 15분 이상 활동 없으면 타임아웃 처리
                                if (currentTime - lastActivityTime > 15 * 60 * 1000) {
                                    log.error("명령어 실행 중 15분 이상 출력 없음 - 무응답 상태로 판단: {}", command);
                                    return "NO_OUTPUT_TIMEOUT";
                                }
                            }
                        }

                        // 짧은 주기로 확인
                        Thread.sleep(100);
                    }

                    return "COMPLETED_NORMAL";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.debug("워치독 스레드 인터럽트됨: {}", command);
                    return "INTERRUPTED";
                } catch (Exception e) {
                    log.error("워치독 스레드 오류: {}", e.getMessage());
                    return "WATCHDOG_ERROR";
                }
            });

            // 4. 채널 연결
            channel.connect(30 * 1000);  // 30초 연결 타임아웃

            byte[] buffer = new byte[1024];
            long lastActivityTime = System.currentTimeMillis();

            // 5. 출력 모니터링 루프
            while (true) {
                // 5.1. 워치독 스레드 상태 확인
                if (watchdog.isDone()) {
                    try {
                        String result = watchdog.get();
                        if (!"COMPLETED_NORMAL".equals(result) && !"COMPLETED".equals(result)) {
                            log.warn("워치독 스레드 비정상 종료 감지: {}", result);
                            throw new IOException("명령 실행 중단: " + result);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("워치독 스레드 결과 확인 오류: {}", e.getMessage());
                        throw new IOException("워치독 스레드 오류", e);
                    }
                }

                // 5.2. 출력 읽기
                boolean hasOutput = false;

                // 5.2.1. 표준 출력 읽기
                while (stdout.available() > 0) {
                    int i = stdout.read(buffer, 0, buffer.length);
                    if (i < 0) break;
                    String output = new String(buffer, 0, i, StandardCharsets.UTF_8);
                    outputBuilder.append(output);
                    log.debug("명령 출력: {}", output);
                    lastActivityTime = System.currentTimeMillis();
                    hasOutput = true;
                }

                // 5.2.2. 오류 출력 읽기
                while (stderr.available() > 0) {
                    int i = stderr.read(buffer, 0, buffer.length);
                    if (i < 0) break;
                    String error = new String(buffer, 0, i, StandardCharsets.UTF_8);
                    outputBuilder.append("[ERROR] ").append(error);
                    log.warn("명령 오류 출력: {}", error);
                    lastActivityTime = System.currentTimeMillis();
                    hasOutput = true;
                }

                // 5.3. 채널 종료 확인
                if (channel.isClosed()) {
                    int exitStatus = channel.getExitStatus();

                    // 마지막 출력 읽기
                    while (stdout.available() > 0) {
                        int i = stdout.read(buffer, 0, buffer.length);
                        if (i < 0) break;
                        outputBuilder.append(new String(buffer, 0, i, StandardCharsets.UTF_8));
                    }

                    while (stderr.available() > 0) {
                        int i = stderr.read(buffer, 0, buffer.length);
                        if (i < 0) break;
                        outputBuilder.append("[ERROR] ").append(new String(buffer, 0, i, StandardCharsets.UTF_8));
                    }

                    // 명령 완료 플래그 설정
                    commandCompleted.set(true);

                    // 종료 상태 확인
                    if (exitStatus != 0 && !command.contains("|| true")) {
                        String errorMsg = String.format("명령 실패 (exit=%d): %s\n%s",
                                exitStatus, command, outputBuilder);
                        log.error(errorMsg);
                        throw new IOException(errorMsg);
                    }

                    break;
                }

                // 5.4. 짧은 대기 (출력이 없는 경우)
                if (!hasOutput) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("명령 대기 중 인터럽트: {}", command);
                        commandCompleted.set(true);
                        throw new IOException("명령 대기 중 인터럽트", e);
                    }
                }
            }

            return outputBuilder.toString();
        } finally {
            // 6. 자원 정리
            if (commandCompleted != null) {
                commandCompleted.set(true);
            }

            if (executor != null) {
                try {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.warn("워치독 스레드가 5초 내에 종료되지 않았습니다.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("워치독 스레드 종료 대기 중 인터럽트 발생");
                }
            }

            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    // [optional] 방화벽 설정
    public List<String> setFirewall() {
        return List.of(
                "sudo ufw enable",
                "sudo ufw allow 22",
                "sudo ufw allow 80",
                "sudo ufw allow 443",
                "sudo ufw allow 8080",
                "sudo ufw allow 9090",
                "sudo ufw allow 3306",
                "sudo ufw reload",
                "sudo ufw status"
        );
    }

    // 1. 스왑 메모리 설정
    public void setSwapMemory(Session sshSession, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.SET_SWAP_MEMORY);

        List<String> cmds = List.of(
                // 기존 파일 제거
                "if [ -f /swapfile ]; then sudo swapoff /swapfile; fi",
                "sudo sed -i '/\\/swapfile/d' /etc/fstab",
                "sudo rm -f /swapfile",
                "free -h",

                // 스왑 메모리 설정
                "sudo fallocate -l 4G /swapfile",
                "sudo chmod 600 /swapfile",
                "sudo mkswap /swapfile",
                "sudo swapon /swapfile",
                "echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab"
        );

        log.info("1. 메모리 스왑 스크립트 실행");
        execCommands(sshSession, cmds);
    }

    // 2. 패키지 업데이트
    public void updatePackageManager(Session sshSession, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.UPDATE_PACKAGE);

        List<String> cmds = List.of(
                "sudo apt update && sudo apt upgrade -y",
                waitForAptLock(),
                "sudo timedatectl set-timezone Asia/Seoul"
        );

        log.info("2. 메모리 스왑 설정");
        execCommands(sshSession, cmds);
    }

    // 3. JDK 설치
    public void installJDK(Session sshSession, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_JDK);

        List<String> cmds = List.of(
                "sudo apt install -y openjdk-17-jdk",
                waitForAptLock(),
                "java -version"
        );

        log.info("3. JDK 설치");
        execCommands(sshSession, cmds);
    }

    // 4. Docker 설치
    public void installDocker(Session sshSession, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_DOCKER);

        List<String> cmds = List.of(
                // 5-1. 공식 GPG 키 추가
                "sudo apt install -y ca-certificates curl gnupg",
                waitForAptLock(),
                "sudo install -m 0755 -d /etc/apt/keyrings",
                "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --batch --yes --no-tty --dearmor -o /etc/apt/keyrings/docker.gpg",

                // 5-2. Docker 레포지토리 등록
                "echo \\\n" +
                        "  \"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \\\n" +
                        "  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo \\\"$VERSION_CODENAME\\\") stable\" | \\\n" +
                        "  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null",

                // 5-3. Docker 설치
                "sudo apt update",
                waitForAptLock(),
                "sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin",
                waitForAptLock(),

                // 5-6. systemd 오버라이드 파일 생성
                "sudo mkdir -p /etc/systemd/system/docker.service.d",
                "sudo tee /etc/systemd/system/docker.service.d/override.conf > /dev/null << 'EOF'\n" +
                        "[Service]\n" +
                        "ExecStart=\n" +
                        "ExecStart=/usr/bin/dockerd -H fd:// -H unix:///var/run/docker.sock -H tcp://0.0.0.0:3789 --containerd=/run/containerd/containerd.sock\n" +
                        "EOF\n",

                // 5-7. Docker 서비스 재시작
                "sudo systemctl daemon-reload",
                "sudo systemctl enable docker",
                "sudo systemctl restart docker",
                "sudo docker network create mynet || true"
        );

        log.info("4. Docker 설치");
        execCommands(sshSession, cmds);
    }

    // 5. 사용자 지정 어플리케이션 실행 with Docker
    public void runApplicationList(Session sshSession, Project project, byte[] backendEnvFile) {
        serverStatusService.updateStatus(project, ServerStatus.RUN_APPLICATION);

        List<ProjectApplication> projectApplicationList = projectApplicationRepository.findAllByProjectId(project.getId());

        try {
            Map<String, String> envMap = parseEnvFile(backendEnvFile);

            List<String> cmds = projectApplicationList.stream()
                    .flatMap(app -> {

                        Application application = applicationRepository.findById(app.getApplicationId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

                        String image = app.getImageName();
                        int port = app.getPort();
                        String tag = app.getTag();
                        String defaultTag = application.getDefaultTag() != null
                                ? application.getDefaultTag()
                                : tag;

                        // stop, rm 명령
                        String stop = "sudo docker stop " + image + " || true";
                        String rm   = "sudo docker rm "   + image + " || true";

                        // run 명령 빌드
                        StringBuilder runSb = new StringBuilder();
                        runSb.append("sudo docker run -d ")
                                .append("--restart unless-stopped ")
                                .append("--network mynet ")
                                .append("--name ").append(image).append(" ")
                                .append("-p ").append(port).append(":").append(port).append(" ");

                        List<String> applicationEnvList = applicationEnvVariableListRepository.findEnvVariablesByApplicationId(app.getApplicationId());

                        if (applicationEnvList != null && !applicationEnvList.isEmpty()) {
                            for (String key : applicationEnvList) {
                                String value = envMap.get(key);
                                if (value != null) {
                                    runSb.append("-e ")
                                            .append(key)
                                            .append("=")
                                            .append(value)
                                            .append(" ");
                                } else {
                                    // 필요 시, 값이 없을 때 로그 출력 또는 예외 처리
                                    System.out.println("Warning: .env 파일에 " + key + " 값이 없습니다.");
                                }
                            }
                        }

                        // 마지막에 이미지:태그
                        runSb.append(image).append(":").append(defaultTag);

                        String run = runSb.toString();

                        return Stream.of(stop, rm, run);
                    })
                    .toList();

            log.info("5. 사용자 지정 어플리케이션 실행");
            execCommands(sshSession, cmds);

        } catch (IOException | JSchException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> parseEnvFile(byte[] envFileBytes) throws IOException {
        Map<String, String> envMap = new HashMap<>();
        String content = new String(envFileBytes, StandardCharsets.UTF_8);

        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    envMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return envMap;
    }

    // 6. Nginx 설치
    public void installNginx(Session sshSession, Project project, String serverIp) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_NGINX);

        String nginxConf = String.format("""
            server {
                listen 80;
                server_name %s;
        
                location / {
                    proxy_pass http://localhost:3000;
                    proxy_http_version 1.1;
                    proxy_set_header Upgrade $http_upgrade;
                    proxy_set_header Connection 'upgrade';
                    proxy_set_header Host $host;
                    proxy_cache_bypass $http_upgrade;
                }
        
                location /api/ {
                    proxy_pass http://localhost:8080/api/;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    proxy_set_header X-Forwarded-Proto $scheme;
                }
        
                location /swagger-ui/ {
                    proxy_pass http://localhost:8080/swagger-ui/;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                }
        
                location /v3/api-docs {
                    proxy_pass http://localhost:8080/v3/api-docs;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    add_header Access-Control-Allow-Origin *;
                }
        
                location /ws {
                    proxy_pass http://localhost:8080/ws;
                    proxy_http_version 1.1;
                    proxy_set_header Upgrade $http_upgrade;
                    proxy_set_header Connection "upgrade";
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    proxy_read_timeout 86400;
                }
            }
            """, serverIp);

        List<String> cmds = List.of(
                // 7-1. Nginx 설치
                "sudo apt install -y nginx",
                waitForAptLock(),
                "sudo systemctl enable nginx",
                "sudo systemctl start nginx",

                // 7-2. app.conf 생성 (with IP)
                "sudo tee /etc/nginx/sites-available/app.conf > /dev/null << 'EOF'\n" +
                        nginxConf +
                        "EOF",

                // 7-3. 심볼릭 링크 생성
                "sudo ln -sf /etc/nginx/sites-available/app.conf /etc/nginx/sites-enabled/app.conf",

                // 7-4. 기존 default 링크 제거
                "sudo rm -f /etc/nginx/sites-enabled/default",

                // 7-5. 설정 테스트 및 적용
                "sudo nginx -t",
                "sudo systemctl reload nginx"
        );

        log.info("6. Nginx 설치");
        execCommands(sshSession, cmds);
    }

    // 8. Jenkins 설치
    public void installJenkins(Session sshSession, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_JENKINS);

        List<String> cmds = List.of(
                "sudo mkdir -p /usr/share/keyrings",
                "curl -fsSL https://pkg.jenkins.io/debian/jenkins.io-2023.key | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null",
                "echo 'deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian binary/' | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null",
                "sudo apt update",
                waitForAptLock(),
//                "sudo apt install -y --allow-downgrades jenkins=2.504",
                "sudo apt install -y jenkins",
                waitForAptLock()
        );

        log.info("8. Jenkins 설치");
        execCommands(sshSession, cmds);
    }

    // 9. Jenkins 사용자 등록 / 플러그인 설치
    public void installJenkinsPlugins(Session sshSession, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_JENKINS_PLUGINS);

        List<String> cmds = List.of(
                // 기본 폴더 초기화
                "sudo rm -rf /var/lib/jenkins/*",

                // Setup Wizard 비활성화 및 포트 변경
                "sudo sed -i '/ExecStart/ c\\ExecStart=/usr/bin/java -Djava.awt.headless=true -Djenkins.install.runSetupWizard=false -jar /usr/share/java/jenkins.war --httpPort=9090 --argumentsRealm.passwd.admin=pwd123 --argumentsRealm.roles.admin=admin' /lib/systemd/system/jenkins.service",
                "sudo systemctl daemon-reload",
                "sudo systemctl restart jenkins",

                // admin 사용자 등록
                "sudo mkdir -p /var/lib/jenkins/users/admin",
                "sudo tee /var/lib/jenkins/users/admin/config.xml > /dev/null <<EOF\n" +
                        "<?xml version='1.1' encoding='UTF-8'?>\n" +
                        "<user>\n" +
                        "  <fullName>admin</fullName>\n" +
                        "  <properties>\n" +
                        "    <hudson.security.HudsonPrivateSecurityRealm_-Details>\n" +
                        "      <passwordHash>#jbcrypt:$2b$12$6CPsRl/Dz/hQRDDoMCyUyuk.q3QsYwnsH8cSzi/43H1ybVsn4yBva</passwordHash>\n" +
                        "    </hudson.security.HudsonPrivateSecurityRealm_-Details>\n" +
                        "  </properties>\n" +
                        "</user>\n" +
                        "EOF" ,

                "sudo mkdir -p /var/lib/jenkins/init.groovy.d",
                "sudo tee /var/lib/jenkins/init.groovy.d/init_token.groovy > /dev/null <<EOF\n" +
                        "import jenkins.model.*\n" +
                        "import jenkins.security.ApiTokenProperty\n" +
                        "def instance = Jenkins.get()\n" +
                        "def user = instance.getUser(\"admin\")\n" +
                        "if (user == null) {\n" +
                        "    println(\"[INIT] Jenkins user 'admin' not found.\")\n" +
                        "} else {\n" +
                        "    def token = user.getProperty(ApiTokenProperty.class).getTokenStore().generateNewToken(\"init-token\")\n" +
                        "    println(\"[INIT] Jenkins API Token: \" + token.plainValue)\n" +
                        "    new File(\"/tmp/jenkins_token\").text = token.plainValue\n" +
                        "}\n" +
                        "EOF",

                "sudo chown -R jenkins:jenkins /var/lib/jenkins/users",
                "sudo chown -R jenkins:jenkins /var/lib/jenkins/init.groovy.d",

                "curl -L https://github.com/jenkinsci/plugin-installation-manager-tool/releases/download/2.12.13/jenkins-plugin-manager-2.12.13.jar -o ~/jenkins-plugin-cli.jar",
                "sudo systemctl stop jenkins",

                // 기존 코드와 동일하게 플러그인 설치
                "sudo mkdir -p /var/lib/jenkins/plugins",
                "cd /tmp",
                "wget https://a609-betty-bucket.s3.ap-northeast-2.amazonaws.com/jenkins/plugins/plugins-cache.tar.gz",
                "tar xzf plugins-cache.tar.gz",
                "sudo cp *.jpi /var/lib/jenkins/plugins/",

                "sudo chown -R jenkins:jenkins /var/lib/jenkins/plugins",
                "sudo usermod -aG docker jenkins",
                "sudo systemctl daemon-reload",
                "sudo systemctl restart jenkins"
        );

        log.info("9. Jenkins 사용자 등록 및 플러그인 설치");
        execCommands(sshSession, cmds);
    }

    // 10. Jenkins Configuration 설정 (PAT 등록, 환경변수 등록)
    public void setJenkinsConfiguration(Session sshSession, Project project, String gitlabUsername, String gitlabToken, byte[] frontEnvFile, byte[] backEnvFile) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.SET_JENKINS_INFO);

        String frontEnvFileStr = Base64.getEncoder().encodeToString(frontEnvFile);
        String backEnvFileStr = Base64.getEncoder().encodeToString(backEnvFile);

        List<String> cmds = List.of(
                // CLI 다운로드
                "wget http://localhost:9090/jnlpJars/jenkins-cli.jar",

                // GitLab Personal Access Token 등록
                "cat <<EOF | java -jar jenkins-cli.jar -s http://localhost:9090/ -auth admin:pwd123 create-credentials-by-xml system::system::jenkins _\n" +
                        "<com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>\n" +
                        "  <scope>GLOBAL</scope>\n" +
                        "  <id>gitlab-token</id>\n" +
                        "  <description>GitLab token</description>\n" +
                        "  <username>" + gitlabUsername + "</username>\n" +
                        "  <password>" + gitlabToken + "</password>\n" +
                        "</com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>\n" +
                        "EOF",

                // 백엔드 환경변수 등록 (파일 기반)
                "cat <<EOF | java -jar jenkins-cli.jar -s http://localhost:9090/ -auth admin:pwd123 create-credentials-by-xml system::system::jenkins _\n" +
                        "<org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl>\n" +
                        "  <scope>GLOBAL</scope>\n" +
                        "  <id>backend</id>\n" +
                        "  <description></description>\n" +
                        "  <fileName>.env</fileName>\n" +
                        "  <secretBytes>" + backEnvFileStr + "</secretBytes>\n" +
                        "</org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl>\n" +
                        "EOF",

                // 프론트엔드 환경변수 등록 (파일 기반)
                "cat <<EOF | java -jar jenkins-cli.jar -s http://localhost:9090/ -auth admin:pwd123 create-credentials-by-xml system::system::jenkins _\n" +
                        "<org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl>\n" +
                        "  <scope>GLOBAL</scope>\n" +
                        "  <id>frontend</id>\n" +
                        "  <description></description>\n" +
                        "  <fileName>.env</fileName>\n" +
                        "  <secretBytes>" + frontEnvFileStr + "</secretBytes>\n" +
                        "</org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl>\n" +
                        "EOF"
        );

        log.info("10. Jenkins Configuration 설정 (PAT 등록, 환경변수 등록)");
        execCommands(sshSession, cmds);
    }

    // 11. Jenkins Pipeline 설정
    public void createJenkinsPipeline(Session sshSession, Project project, String jobName, String gitRepoUrl, String credentialsId, String gitlabTargetBranchName) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.CREATE_JENKINS_PIPELINE);

        String jobConfigXml = String.join("\n",
                "sudo tee job-config.xml > /dev/null <<EOF",
                "<?xml version='1.1' encoding='UTF-8'?>",
                "<flow-definition plugin=\"workflow-job\">",
                "  <description>GitLab 연동 자동 배포</description>",
                "  <keepDependencies>false</keepDependencies>",
                "  <properties>",
                "    <hudson.model.ParametersDefinitionProperty>",
                "      <parameterDefinitions>",
                "        <hudson.model.StringParameterDefinition>",
                "          <name>BRANCH_NAME</name>",
                "          <defaultValue>" + gitlabTargetBranchName + "</defaultValue>",
                "          <description>Git 브랜치 이름</description>",
                "        </hudson.model.StringParameterDefinition>",
                "      </parameterDefinitions>",
                "    </hudson.model.ParametersDefinitionProperty>",
                "  </properties>",
                "  <definition class=\"org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition\" plugin=\"workflow-cps\">",
                "    <scm class=\"hudson.plugins.git.GitSCM\" plugin=\"git\">",
                "      <configVersion>2</configVersion>",
                "      <userRemoteConfigs>",
                "        <hudson.plugins.git.UserRemoteConfig>",
                "          <url>" + gitRepoUrl + "</url>",
                "          <credentialsId>" + credentialsId + "</credentialsId>",
                "        </hudson.plugins.git.UserRemoteConfig>",
                "      </userRemoteConfigs>",
                "      <branches>",
                "        <hudson.plugins.git.BranchSpec>",
                "          <name>" + gitlabTargetBranchName + "</name>",
                "        </hudson.plugins.git.BranchSpec>",
                "      </branches>",
                "    </scm>",
                "    <scriptPath>Jenkinsfile</scriptPath>",
                "    <lightweight>true</lightweight>",
                "  </definition>",
                "  <triggers>",
                "    <com.dabsquared.gitlabjenkins.GitLabPushTrigger plugin=\"gitlab-plugin\">",
                "      <spec></spec>",
                "      <triggerOnPush>true</triggerOnPush>",
                "      <triggerOnMergeRequest>false</triggerOnMergeRequest>",
                "      <triggerOnNoteRequest>false</triggerOnNoteRequest>",
                "      <triggerOnPipelineEvent>false</triggerOnPipelineEvent>",
                "      <triggerOnAcceptedMergeRequest>false</triggerOnAcceptedMergeRequest>",
                "      <triggerOnClosedMergeRequest>false</triggerOnClosedMergeRequest>",
                "      <triggerOnApprovedMergeRequest>false</triggerOnApprovedMergeRequest>",
                "      <triggerOpenMergeRequestOnPush>never</triggerOpenMergeRequestOnPush>",
                "      <ciSkip>false</ciSkip>",
                "      <setBuildDescription>true</setBuildDescription>",
                "      <addNoteOnMergeRequest>false</addNoteOnMergeRequest>",
                "      <addVoteOnMergeRequest>false</addVoteOnMergeRequest>",
                "      <useCiFeatures>false</useCiFeatures>",
                "      <addCiMessage>false</addCiMessage>",
                "      <branchFilterType>All</branchFilterType>",
                "    </com.dabsquared.gitlabjenkins.GitLabPushTrigger>",
                "  </triggers>",
                "</flow-definition>",
                "EOF"
        );

        List<String> cmds = List.of(
                jobConfigXml,
                "wget http://localhost:9090/jnlpJars/jenkins-cli.jar",
                "java -jar jenkins-cli.jar -s http://localhost:9090/ -auth admin:pwd123 create-job " + jobName + " < job-config.xml"
        );

        log.info("11. Jenkins Pipeline 생성");
        execCommands(sshSession, cmds);
    }

    // 12. Jenkinsfile 생성
    public void createJenkinsFile(Session sshSession, String repositoryUrl, String projectPath, String projectName, String gitlabTargetBranchName, String namespace, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.CREATE_JENKINSFILE);

        String frontendDockerScript;

        switch (project.getFrontendFramework()) {
            case "Vue.js":
                frontendDockerScript =
                        "                        set -e\n" +
                                "                        docker build -f Dockerfile -t vue .\n" +
                                "                        docker stop vue || true\n" +
                                "                        docker rm vue || true\n" +
                                "                        docker run -d --network mynet  --env-file .env --restart unless-stopped --name vue -p 3000:3000 vue\n";
                break;

            case "React":
                frontendDockerScript =
                        "                        set -e\n" +
                                "                        docker build -f Dockerfile -t react .\n" +
                                "                        docker stop react || true\n" +
                                "                        docker rm react || true\n" +
                                "                        docker run -d --network mynet --env-file .env --restart unless-stopped --name react -p 3000:3000 react\n";
                break;

            case "Next.js":
            default:
                frontendDockerScript =
                        "                        set -e\n" +
                                "                        docker build -f Dockerfile -t next .\n" +
                                "                        docker stop next || true\n" +
                                "                        docker rm next || true\n" +
                                "                        docker run -d --network mynet --env-file .env --restart unless-stopped --name next -p 3000:3000 next\n";
                break;
        }

//        String jenkinsfileContent =
//                "cd " + projectPath + " && sudo tee Jenkinsfile > /dev/null <<'EOF'\n" +
//                        "pipeline {\n" +
//                        "    agent any\n" +
//                        "    parameters {\n" +
//                        "        string(name: 'ORIGINAL_BRANCH_NAME', defaultValue: '" + project.getGitlabTargetBranchName() + "', description: '브랜치 이름')\n" +
//                        "        string(name: 'BRANCH_NAME', defaultValue: '" + project.getGitlabTargetBranchName() + "', description: '브랜치 이름')\n" +
//                        "        string(name: 'PROJECT_ID', defaultValue: '" + project.getId() + "', description: '프로젝트 ID')\n" +
//                        "    }\n" +
//                        "    stages {\n" +
//                        "        stage('Checkout') {\n" +
//                        "            steps {\n" +
//                        "                echo '1. 워크스페이스 정리 및 소스 체크아웃'\n" +
//                        "                deleteDir()\n" +
//                        "                withCredentials([usernamePassword(credentialsId: 'gitlab-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {\n" +
//                        "                    git branch: \"${params.BRANCH_NAME}\", url: \"https://${GIT_USER}:${GIT_TOKEN}@lab.ssafy.com/" + namespace + ".git\"\n" +
//                        "                }\n" +
//                        "            }\n" +
//                        "        }\n" +
//                        "        stage('변경 감지') {\n" +
//                        "            steps {\n" +
//                        "                script {\n" +
//                        "                    // 기본 빌드 상태 초기화\n" +
//                        "                    env.BACKEND_BUILD_STATUS = 'NOT_EXECUTED'\n" +
//                        "                    env.FRONTEND_BUILD_STATUS = 'NOT_EXECUTED'\n" +
//                        "                    env.HEALTH_CHECK_STATUS = 'NOT_EXECUTED'\n" +
//                        "                    \n" +
//                        "                    // 첫 번째 빌드인지 확인\n" +
//                        "                    def isFirstBuild = currentBuild.previousBuild == null\n" +
//                        "                    \n" +
//                        "                    if (isFirstBuild) {\n" +
//                        "                        echo \"🔵 첫 번째 빌드 → 전체 빌드 실행\"\n" +
//                        "                        env.BACKEND_CHANGED = \"true\"\n" +
//                        "                        env.FRONTEND_CHANGED = \"true\"\n" +
//                        "                        return\n" +
//                        "                    }\n" +
//                        "                    \n" +
//                        "                    sh \"git fetch origin ${params.BRANCH_NAME} --quiet\"\n" +
//                        "                    def hasBase = sh(\n" +
//                        "                        script: \"git merge-base origin/${params.BRANCH_NAME} HEAD > /dev/null 2>&1 && echo yes || echo no\",\n" +
//                        "                        returnStdout: true\n" +
//                        "                    ).trim()\n" +
//                        "                    if (hasBase == \"no\") {\n" +
//                        "                        echo \"🟡 기준 브랜치와 공통 커밋 없음 → 전체 빌드 실행\"\n" +
//                        "                        env.BACKEND_CHANGED = \"true\"\n" +
//                        "                        env.FRONTEND_CHANGED = \"true\"\n" +
//                        "                        return\n" +
//                        "                    }\n" +
//                        "                    def changedFiles = sh(\n" +
//                        "                        script: \"git diff --name-only origin/${params.BRANCH_NAME}...HEAD\",\n" +
//                        "                        returnStdout: true\n" +
//                        "                    ).trim()\n" +
//                        "                    echo \"🔍 변경된 파일 목록:\\n${changedFiles}\"\n" +
//                        "                    env.BACKEND_CHANGED  = changedFiles.contains(\"backend/\")  ? \"true\" : \"false\"\n" +
//                        "                    env.FRONTEND_CHANGED = changedFiles.contains(\"frontend/\") ? \"true\" : \"false\"\n" +
//                        "                    if (env.BACKEND_CHANGED == \"false\" && env.FRONTEND_CHANGED == \"false\") {\n" +
//                        "                        echo \"⚠️ 변경된 파일 없음 → 재시도 빌드일 수 있으므로 전체 빌드 강제 실행\"\n" +
//                        "                        env.BACKEND_CHANGED = \"true\"\n" +
//                        "                        env.FRONTEND_CHANGED = \"true\"\n" +
//                        "                    }\n" +
//                        "                    echo \"🛠️ 백엔드 변경됨: ${env.BACKEND_CHANGED}\"\n" +
//                        "                    echo \"🎨 프론트엔드 변경됨: ${env.FRONTEND_CHANGED}\"\n" +
//                        "                }\n" +
//                        "            }\n" +
//                        "        }\n" +
//                        "        stage('Build Backend') {\n" +
//                        "            when {\n" +
//                        "                expression { env.BACKEND_CHANGED == \"true\" }\n" +
//                        "            }\n" +
//                        "            steps {\n" +
//                        "                script {\n" +
//                        "                    env.BACKEND_BUILD_STATUS = 'SUCCESS'\n" +
//                        "                    try {\n" +
//                        "                        withCredentials([file(credentialsId: \"backend\", variable: 'BACKEND_ENV')]) {\n" +
//                        "                            sh '''\n" +
//                        "                                cp \"$BACKEND_ENV\" \"$WORKSPACE/backend/.env\"\n" +
//                        "                            '''\n" +
//                        "                        }\n" +
//                        "                        dir('backend') {\n" +
//                        "                            sh '''\n" +
//                        "                                docker build -t spring .\n" +
//                        "                                docker stop spring || true\n" +
//                        "                                docker rm spring || true\n" +
//                        "                                docker run -d -p 8080:8080 --network mynet --env-file .env --name spring spring\n" +
//                        "                            '''\n" +
//                        "                        }\n" +
//                        "                    } catch (Exception e) {\n" +
//                        "                        env.BACKEND_BUILD_STATUS = 'FAILED'\n" +
//                        "                        echo \"❌ 백엔드 빌드 실패: ${e.message}\"\n" +
//                        "                        throw e\n" +
//                        "                    }\n" +
//                        "                }\n" +
//                        "            }\n" +
//                        "        }\n" +
//                        "        stage('Build Frontend') {\n" +
//                        "            when {\n" +
//                        "                expression { env.FRONTEND_CHANGED == \"true\" }\n" +
//                        "            }\n" +
//                        "            steps {\n" +
//                        "                script {\n" +
//                        "                    env.FRONTEND_BUILD_STATUS = 'SUCCESS'\n" +
//                        "                    try {\n" +
//                        "                        withCredentials([file(credentialsId: \"frontend\", variable: 'FRONTEND_ENV')]) {\n" +
//                        "                            sh '''\n" +
//                        "                                cp \"$FRONTEND_ENV\" \"$WORKSPACE/frontend/.env\"\n" +
//                        "                            '''\n" +
//                        "                        }\n" +
//                        "                        dir('frontend') {\n" +
//                        "                            sh '''\n" +
//                        "                                " + frontendDockerScript + "\n" +
//                        "                            '''\n" +
//                        "                        }\n" +
//                        "                    } catch (Exception e) {\n" +
//                        "                        env.FRONTEND_BUILD_STATUS = 'FAILED'\n" +
//                        "                        echo \"❌ 프론트엔드 빌드 실패: ${e.message}\"\n" +
//                        "                        throw e\n" +
//                        "                    }\n" +
//                        "                }\n" +
//                        "            }\n" +
//                        "        }\n" +
//                        "        stage('Health Check') {\n" +
//                        "            steps {\n" +
//                        "                // Health Check 전에 30초 대기\n" +
//                        "                echo '⏳ Health Check 전에 30초 대기'\n" +
//                        "                sleep time: 30, unit: 'SECONDS'\n" +
//                        "                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {\n" +
//                        "                    script {\n" +
//                        "                        // 헬스 체크 로직 추가\n" +
//                        "                        echo '⚕️ 서비스 헬스 체크 실행'\n" +
//                        "                        env.HEALTH_CHECK_STATUS = 'SUCCESS' // 기본값 설정\n" +
//                        "                        \n" +
//                        "                        // Docker API를 통한 컨테이너 상태 확인 URL\n" +
//                        "                        def dockerApiUrl = 'http://localhost:3789/containers/json?all=true&filters=%7B%22name%22%3A%5B%22spring%22%5D%7D'\n" +
//                        "                        \n" +
//                        "                        try {\n" +
//                        "                            // Docker API 호출\n" +
//                        "                            def dockerApiResponse = sh(script: \"\"\"\n" +
//                        "                                curl -s -X GET '${dockerApiUrl}'\n" +
//                        "                            \"\"\", returnStdout: true).trim()\n" +
//                        "                            \n" +
//                        "                            echo \"Docker API 응답: ${dockerApiResponse}\"\n" +
//                        "                            \n" +
//                        "                            // JSON 응답 파싱\n" +
//                        "                            def jsonSlurper = new groovy.json.JsonSlurper()\n" +
//                        "                            def containers\n" +
//                        "                            try {\n" +
//                        "                                containers = jsonSlurper.parseText(dockerApiResponse)\n" +
//                        "                            } catch (Exception e) {\n" +
//                        "                                echo \"JSON 파싱 오류: ${e.message}\"\n" +
//                        "                                env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
//                        "                                error \"헬스 체크 실패: JSON 파싱 오류\"\n" +
//                        "                            }\n" +
//                        "                            \n" +
//                        "                            // 컨테이너 목록 확인\n" +
//                        "                            if (containers instanceof List) {\n" +
//                        "                                if (containers.size() == 0) {\n" +
//                        "                                    echo \"❌ 헬스 체크 실패: spring 컨테이너를 찾을 수 없습니다.\"\n" +
//                        "                                    env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
//                        "                                    error \"헬스 체크 실패: spring 컨테이너를 찾을 수 없습니다.\"\n" +
//                        "                                }\n" +
//                        "                                \n" +
//                        "                                // 컨테이너 상태 확인\n" +
//                        "                                def springContainer = containers[0]\n" +
//                        "                                def containerState = springContainer.State\n" +
//                        "                                def containerStatus = springContainer.Status\n" +
//                        "                                \n" +
//                        "                                echo \"컨테이너 상태: ${containerState}, 상태 설명: ${containerStatus}\"\n" +
//                        "                                \n" +
//                        "                                // 'running' 상태인지 확인\n" +
//                        "                                if (containerState == 'running') {\n" +
//                        "                                    echo \"✅ 헬스 체크 성공: spring 컨테이너가 정상 실행 중입니다.\"\n" +
//                        "                                    env.HEALTH_CHECK_STATUS = 'SUCCESS'\n" +
//                        "                                } else {\n" +
//                        "                                    echo \"❌ 헬스 체크 실패: spring 컨테이너 상태가 '${containerState}'입니다.\"\n" +
//                        "                                    env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
//                        "                                    error \"헬스 체크 실패: spring 컨테이너 상태가 '${containerState}'입니다.\"\n" +
//                        "                                }\n" +
//                        "                            } else {\n" +
//                        "                                echo \"❌ 헬스 체크 실패: Docker API 응답이 리스트 형식이 아닙니다.\"\n" +
//                        "                                env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
//                        "                                error \"헬스 체크 실패: Docker API 응답이 리스트 형식이 아닙니다.\"\n" +
//                        "                            }\n" +
//                        "                        } catch (Exception e) {\n" +
//                        "                            echo \"❌ 헬스 체크 실행 중 오류 발생: ${e.message}\"\n" +
//                        "                            env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
//                        "                            throw e\n" +
//                        "                        }\n" +
//                        "                    }\n" +
//                        "                }\n" +
//                        "            }\n" +
//                        "        }\n" +
//                        "    }\n" +
//                        "    post {\n" +
//                        "        always {\n" +
//                        "            script {\n" +
//                        "                // 빌드 결과 상태 가져오기\n" +
//                        "                def buildStatus = currentBuild.result ?: 'SUCCESS'\n" +
//                        "                env.SELF_HEALING_APPLIED = 'false'  // 셀프 힐링 적용 여부를 추적하는 변수\n" +
//                        "                \n" +
//                        "                // PROJECT_ID 파라미터가 비어있지 않은지 확인\n" +
//                        "                if (params.PROJECT_ID?.trim()) {\n" +
//                        "                    withCredentials([usernamePassword(credentialsId: 'gitlab-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {\n" +
//                        "                        // API 기본 URL 설정\n" +
//                        "                        def apiBaseUrl = 'https://seedinfra.store/api'\n" +
//                        "                        \n" +
//                        "                        // 셀프 힐링 API 호출 함수 정의\n" +
//                        "                        def callSelfHealingApi = { failType ->\n" +
//                        "                            def healingApiUrl = \"${apiBaseUrl}/self-cicd/resolve\"\n" +
//                        "                            def queryParams = \"projectId=${params.PROJECT_ID}&personalAccessToken=${GIT_TOKEN}&failType=${failType}\"\n" +
//                        "                            \n" +
//                        "                            try {\n" +
//                        "                                def healingResponse = sh(script: \"\"\"\n" +
//                        "                                    curl -X POST \\\n" +
//                        "                                    -H 'Content-Type: application/json' \\\n" +
//                        "                                    -w '\\n%{http_code}' \\\n" +
//                        "                                    \"${healingApiUrl}?${queryParams}\" \n" +
//                        "                                \"\"\", returnStdout: true).trim()\n" +
//                        "                                \n" +
//                        "                                echo \"셀프 힐링 API 호출 결과 (${failType}): ${healingResponse}\"\n" +
//                        "                                env.SELF_HEALING_APPLIED = 'true'\n" +
//                        "                            } catch (Exception e) {\n" +
//                        "                                echo \"셀프 힐링 API 호출 실패 (${failType}): ${e.message}\"\n" +
//                        "                            }\n" +
//                        "                        }\n" +
//                        "                        \n" +
//                        "                        // 셀프 힐링 API 호출 조건 확인\n" +
//                        "                        if (params.BRANCH_NAME == params.ORIGINAL_BRANCH_NAME && currentBuild.number > 1) {\n" +
//                        "                            // 빌드 상태 변수 확인 (안전하게 처리)\n" +
//                        "                            def frontendFailed = (env.FRONTEND_BUILD_STATUS == 'FAILED')\n" +
//                        "                            def backendFailed = (env.BACKEND_BUILD_STATUS == 'FAILED')\n" +
//                        "                            def healthCheckFailed = (env.HEALTH_CHECK_STATUS == 'FAILED')\n" +
//                        "                            \n" +
//                        "                            // 변경되지 않아 실행되지 않은 경우 처리\n" +
//                        "                            if (env.FRONTEND_CHANGED == 'false') {\n" +
//                        "                                frontendFailed = false\n" +
//                        "                                echo \"ℹ️ 프론트엔드는 변경되지 않아 빌드가 실행되지 않았습니다.\"\n" +
//                        "                            }\n" +
//                        "                            if (env.BACKEND_CHANGED == 'false') {\n" +
//                        "                                backendFailed = false\n" +
//                        "                                echo \"ℹ️ 백엔드는 변경되지 않아 빌드가 실행되지 않았습니다.\"\n" +
//                        "                            }\n" +
//                        "                            \n" +
//                        "                            echo \"📊 빌드 상태 요약:\\n- 프론트엔드: ${frontendFailed ? '❌ 실패' : '✅ 성공'}\\n- 백엔드: ${backendFailed ? '❌ 실패' : '✅ 성공'}\\n- 헬스 체크: ${healthCheckFailed ? '❌ 실패' : '✅ 성공'}\"\n" +
//                        "                            \n" +
//                        "                            // 케이스 1: 프론트엔드 빌드 실패, 백엔드 빌드 성공, 헬스 체크 성공\n" +
//                        "                            if (frontendFailed && !backendFailed && !healthCheckFailed) {\n" +
//                        "                                echo \"🛠️ 케이스 1: 프론트엔드 빌드 실패 → 셀프 힐링 API 호출 (BUILD)\"\n" +
//                        "                                callSelfHealingApi('BUILD')\n" +
//                        "                            }\n" +
//                        "                            // 케이스 2: 프론트엔드 빌드 실패, 백엔드 빌드 성공, 헬스 체크 실패\n" +
//                        "                            else if (frontendFailed && !backendFailed && healthCheckFailed) {\n" +
//                        "                                echo \"🛠️ 케이스 2: 프론트엔드 빌드 실패 및 헬스 체크 실패 → 셀프 힐링 API 호출 (RUNTIME)\"\n" +
//                        "                                callSelfHealingApi('RUNTIME')\n" +
//                        "                            }\n" +
//                        "                            // 케이스 3: 프론트엔드 빌드 성공, 백엔드 빌드 성공, 헬스 체크 성공\n" +
//                        "                            else if (!frontendFailed && !backendFailed && !healthCheckFailed) {\n" +
//                        "                                echo \"✅ 케이스 3: 모든 빌드 및 헬스 체크 성공 → 셀프 힐링 필요 없음\"\n" +
//                        "                            }\n" +
//                        "                            // 케이스 4: 프론트엔드 빌드 성공, 백엔드 빌드 성공, 헬스 체크 실패\n" +
//                        "                            else if (!frontendFailed && !backendFailed && healthCheckFailed) {\n" +
//                        "                                echo \"🛠️ 케이스 4: 헬스 체크 실패 → 셀프 힐링 API 호출 (RUNTIME)\"\n" +
//                        "                                callSelfHealingApi('RUNTIME')\n" +
//                        "                            }\n" +
//                        "                            // 추가 케이스: 백엔드 빌드 실패\n" +
//                        "                            else if (backendFailed) {\n" +
//                        "                                echo \"🛠️ 추가 케이스: 백엔드 빌드 실패 → 셀프 힐링 API 호출 (BUILD)\"\n" +
//                        "                                callSelfHealingApi('BUILD')\n" +
//                        "                            }\n" +
//                        "                            // 예상치 못한 케이스\n" +
//                        "                            else {\n" +
//                        "                                echo \"⚠️ 예상치 못한 상태: 빌드 상태 ${buildStatus}\\n- 정확한 진단을 위해 Jenkins 로그를 확인하세요.\"\n" +
//                        "                                if (buildStatus != 'SUCCESS') {\n" +
//                        "                                    echo \"❌ 빌드 실패 (기타 케이스) → 셀프 힐링 API 호출 (BUILD)\"\n" +
//                        "                                    callSelfHealingApi('BUILD')\n" +
//                        "                                }\n" +
//                        "                            }\n" +
//                        "                        } else {\n" +
//                        "                            echo \"💬 원본 브랜치와 다른 브랜치 빌드 또는 첫 빌드 → 셀프 힐링 건너뜀\"\n" +
//                        "                        }\n" +
//                        "                        \n" +
//                        "                        // 모든 작업이 완료된 후 마지막으로 빌드 로그 API 호출 (성공/실패 여부 무관)\n" +
//                        "                        echo \"📝 최종 빌드 결과 로깅 API 호출 중: 프로젝트 ID ${params.PROJECT_ID}\"\n" +
//                        "                        \n" +
//                        "                        // 빌드 로그 API 엔드포인트 구성\n" +
//                        "                        def logApiUrl = \"${apiBaseUrl}/jenkins/${params.PROJECT_ID}/log-last-build\"\n" +
//                        "                        \n" +
//                        "                        // 빌드 로그 API 호출 (POST 요청, 빈 본문)\n" +
//                        "                        try {\n" +
//                        "                            def logResponse = sh(script: \"\"\"\n" +
//                        "                                curl -X POST \\\n" +
//                        "                                -H 'Content-Type: application/json' \\\n" +
//                        "                                -w '\\n%{http_code}' \\\n" +
//                        "                                ${logApiUrl}\n" +
//                        "                            \"\"\", returnStdout: true).trim()\n" +
//                        "                            \n" +
//                        "                            echo \"빌드 로그 API 호출 결과: ${logResponse}\"\n" +
//                        "                        } catch (Exception e) {\n" +
//                        "                            echo \"빌드 로그 API 호출 실패: ${e.message}\"\n" +
//                        "                        }\n" +
//                        "                    }\n" +
//                        "                } else {\n" +
//                        "                    echo \"PROJECT_ID 파라미터가 비어있어 API를 호출하지 않습니다.\"\n" +
//                        "                }\n" +
//                        "            }\n" +
//                        "        }\n" +
//                        "    }\n" +
//                        "}\n" +
//                        "EOF\n";

        String jenkinsfileContent =
                "cd " + projectPath + " && sudo tee Jenkinsfile > /dev/null <<'EOF'\n" +
                        "pipeline {\n" +
                        "    agent any\n" +
                        "    parameters {\n" +
                        "        string(name: 'ORIGINAL_BRANCH_NAME', defaultValue: '" + project.getGitlabTargetBranchName() + "', description: '브랜치 이름')\n" +
                        "        string(name: 'BRANCH_NAME', defaultValue: '" + project.getGitlabTargetBranchName() + "', description: '브랜치 이름')\n" +
                        "        string(name: 'PROJECT_ID', defaultValue: '" + project.getId() + "', description: '프로젝트 ID')\n" +
                        "    }\n" +
                        "    stages {\n" +
                        "        stage('Checkout') {\n" +
                        "            steps {\n" +
                        "                echo '1. 워크스페이스 정리 및 소스 체크아웃'\n" +
                        "                deleteDir()\n" +
                        "                withCredentials([usernamePassword(credentialsId: 'gitlab-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {\n" +
                        "                    git branch: \"${params.BRANCH_NAME}\", url: \"https://${GIT_USER}:${GIT_TOKEN}@lab.ssafy.com/" + namespace + ".git\"\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('변경 감지') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    // 기본 빌드 상태 초기화\n" +
                        "                    env.BACKEND_BUILD_STATUS = 'NOT_EXECUTED'\n" +
                        "                    env.FRONTEND_BUILD_STATUS = 'NOT_EXECUTED'\n" +
                        "                    env.HEALTH_CHECK_STATUS = 'NOT_EXECUTED'\n" +
                        "                    \n" +
                        "                    // 첫 번째 빌드인지 확인\n" +
                        "                    def isFirstBuild = currentBuild.previousBuild == null\n" +
                        "                    \n" +
                        "                    if (isFirstBuild) {\n" +
                        "                        echo \"🔵 첫 번째 빌드 → 전체 빌드 실행\"\n" +
                        "                        env.BACKEND_CHANGED = \"true\"\n" +
                        "                        env.FRONTEND_CHANGED = \"true\"\n" +
                        "                        return\n" +
                        "                    }\n" +
                        "                    \n" +
                        "                    sh \"git fetch origin ${params.BRANCH_NAME} --quiet\"\n" +
                        "                    def hasBase = sh(\n" +
                        "                        script: \"git merge-base origin/${params.BRANCH_NAME} HEAD > /dev/null 2>&1 && echo yes || echo no\",\n" +
                        "                        returnStdout: true\n" +
                        "                    ).trim()\n" +
                        "                    if (hasBase == \"no\") {\n" +
                        "                        echo \"🟡 기준 브랜치와 공통 커밋 없음 → 전체 빌드 실행\"\n" +
                        "                        env.BACKEND_CHANGED = \"true\"\n" +
                        "                        env.FRONTEND_CHANGED = \"true\"\n" +
                        "                        return\n" +
                        "                    }\n" +
                        "                    def changedFiles = sh(\n" +
                        "                        script: \"git diff --name-only origin/${params.BRANCH_NAME}...HEAD\",\n" +
                        "                        returnStdout: true\n" +
                        "                    ).trim()\n" +
                        "                    echo \"🔍 변경된 파일 목록:\\n${changedFiles}\"\n" +
                        "                    env.BACKEND_CHANGED  = changedFiles.contains(\"backend/\")  ? \"true\" : \"false\"\n" +
                        "                    env.FRONTEND_CHANGED = changedFiles.contains(\"frontend/\") ? \"true\" : \"false\"\n" +
                        "                    if (env.BACKEND_CHANGED == \"false\" && env.FRONTEND_CHANGED == \"false\") {\n" +
                        "                        echo \"⚠️ 변경된 파일 없음 → 재시도 빌드일 수 있으므로 전체 빌드 강제 실행\"\n" +
                        "                        env.BACKEND_CHANGED = \"true\"\n" +
                        "                        env.FRONTEND_CHANGED = \"true\"\n" +
                        "                    }\n" +
                        "                    echo \"🛠️ 백엔드 변경됨: ${env.BACKEND_CHANGED}\"\n" +
                        "                    echo \"🎨 프론트엔드 변경됨: ${env.FRONTEND_CHANGED}\"\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Build Backend') {\n" +
                        "            when {\n" +
                        "                expression { env.BACKEND_CHANGED == \"true\" }\n" +
                        "            }\n" +
                        "            steps {\n" +
                        "                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {\n" +
                        "                    script {\n" +
                        "                        try {\n" +
                        "                            env.BACKEND_BUILD_STATUS = 'SUCCESS'\n" +
                        "                            withCredentials([file(credentialsId: \"backend\", variable: 'BACKEND_ENV')]) {\n" +
                        "                                sh '''\n" +
                        "                                    cp \"$BACKEND_ENV\" \"$WORKSPACE/backend/.env\"\n" +
                        "                                '''\n" +
                        "                            }\n" +
                        "                            dir('backend') {\n" +
                        "                                sh '''\n" +
                        "                                    docker build -t spring .\n" +
                        "                                    docker stop spring || true\n" +
                        "                                    docker rm spring || true\n" +
                        "                                    docker run -d -p 8080:8080 --network mynet --env-file .env --name spring spring\n" +
                        "                                '''\n" +
                        "                            }\n" +
                        "                        } catch (Exception e) {\n" +
                        "                            env.BACKEND_BUILD_STATUS = 'FAILED'\n" +
                        "                            echo \"❌ 백엔드 빌드 실패: ${e.message}\"\n" +
                        "                            throw e\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }   \n" +
                        "        stage('Build Frontend') {\n" +
                        "            when {\n" +
                        "                expression { env.FRONTEND_CHANGED == \"true\" }\n" +
                        "            }\n" +
                        "            steps {\n" +
                        "                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {\n" +
                        "                    script {\n" +
                        "                        try {\n" +
                        "                            env.FRONTEND_BUILD_STATUS = 'SUCCESS'\n" +
                        "                            withCredentials([file(credentialsId: \"frontend\", variable: 'FRONTEND_ENV')]) {\n" +
                        "                                sh '''\n" +
                        "                                    cp \"$FRONTEND_ENV\" \"$WORKSPACE/frontend/.env\"\n" +
                        "                                '''\n" +
                        "                            }\n" +
                        "                            dir('frontend') {\n" +
                        "                                sh '''\n" +
                        "                                    " + frontendDockerScript + "\n" +
                        "                                '''\n" +
                        "                            }\n" +
                        "                        } catch (Exception e) {\n" +
                        "                            env.FRONTEND_BUILD_STATUS = 'FAILED'\n" +
                        "                            echo \"❌ 프론트엔드 빌드 실패: ${e.message}\"\n" +
                        "                            throw e\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Health Check') {\n" +
                        "            steps {\n" +
                        "                // Health Check 전에 30초 대기\n" +
                        "                echo '⏳ Health Check 전에 30초 대기'\n" +
                        "                sleep time: 30, unit: 'SECONDS'\n" +
                        "                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {\n" +
                        "                    script {\n" +
                        "                        // 헬스 체크 로직 추가\n" +
                        "                        echo '⚕️ 서비스 헬스 체크 실행'\n" +
                        "                        env.HEALTH_CHECK_STATUS = 'SUCCESS' // 기본값 설정\n" +
                        "                        \n" +
                        "                        // Docker API를 통한 컨테이너 상태 확인 URL\n" +
                        "                        def dockerApiUrl = 'http://localhost:3789/containers/json?all=true&filters=%7B%22name%22%3A%5B%22spring%22%5D%7D'\n" +
                        "                        \n" +
                        "                        try {\n" +
                        "                            // Docker API 호출\n" +
                        "                            def dockerApiResponse = sh(script: \"\"\"\n" +
                        "                                curl -s -X GET '${dockerApiUrl}'\n" +
                        "                            \"\"\", returnStdout: true).trim()\n" +
                        "                            \n" +
                        "                            echo \"Docker API 응답: ${dockerApiResponse}\"\n" +
                        "                            \n" +
                        "                            // JSON 응답 파싱\n" +
                        "                            def jsonSlurper = new groovy.json.JsonSlurper()\n" +
                        "                            def containers\n" +
                        "                            try {\n" +
                        "                                containers = jsonSlurper.parseText(dockerApiResponse)\n" +
                        "                            } catch (Exception e) {\n" +
                        "                                echo \"JSON 파싱 오류: ${e.message}\"\n" +
                        "                                env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
                        "                                error \"헬스 체크 실패: JSON 파싱 오류\"\n" +
                        "                            }\n" +
                        "                            \n" +
                        "                            // 컨테이너 목록 확인\n" +
                        "                            if (containers instanceof List) {\n" +
                        "                                if (containers.size() == 0) {\n" +
                        "                                    echo \"❌ 헬스 체크 실패: spring 컨테이너를 찾을 수 없습니다.\"\n" +
                        "                                    env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
                        "                                    error \"헬스 체크 실패: spring 컨테이너를 찾을 수 없습니다.\"\n" +
                        "                                }\n" +
                        "                                \n" +
                        "                                // 컨테이너 상태 확인\n" +
                        "                                def springContainer = containers[0]\n" +
                        "                                def containerState = springContainer.State\n" +
                        "                                def containerStatus = springContainer.Status\n" +
                        "                                \n" +
                        "                                echo \"컨테이너 상태: ${containerState}, 상태 설명: ${containerStatus}\"\n" +
                        "                                \n" +
                        "                                // 'running' 상태인지 확인\n" +
                        "                                if (containerState == 'running') {\n" +
                        "                                    echo \"✅ 헬스 체크 성공: spring 컨테이너가 정상 실행 중입니다.\"\n" +
                        "                                    env.HEALTH_CHECK_STATUS = 'SUCCESS'\n" +
                        "                                } else {\n" +
                        "                                    echo \"❌ 헬스 체크 실패: spring 컨테이너 상태가 '${containerState}'입니다.\"\n" +
                        "                                    env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
                        "                                    error \"헬스 체크 실패: spring 컨테이너 상태가 '${containerState}'입니다.\"\n" +
                        "                                }\n" +
                        "                            } else {\n" +
                        "                                echo \"❌ 헬스 체크 실패: Docker API 응답이 리스트 형식이 아닙니다.\"\n" +
                        "                                env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
                        "                                error \"헬스 체크 실패: Docker API 응답이 리스트 형식이 아닙니다.\"\n" +
                        "                            }\n" +
                        "                        } catch (Exception e) {\n" +
                        "                            echo \"❌ 헬스 체크 실행 중 오류 발생: ${e.message}\"\n" +
                        "                            env.HEALTH_CHECK_STATUS = 'FAILED'\n" +
                        "                            throw e\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "    post {\n" +
                        "        always {\n" +
                        "            script {\n" +
                        "                // 빌드 결과 상태 가져오기\n" +
                        "                def buildStatus = currentBuild.result ?: 'SUCCESS'\n" +
                        "                env.SELF_HEALING_APPLIED = 'false'  // 셀프 힐링 적용 여부를 추적하는 변수\n" +
                        "                \n" +
                        "                // PROJECT_ID 파라미터가 비어있지 않은지 확인\n" +
                        "                if (params.PROJECT_ID?.trim()) {\n" +
                        "                    withCredentials([usernamePassword(credentialsId: 'gitlab-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {\n" +
                        "                        // API 기본 URL 설정\n" +
                        "                        def apiBaseUrl = 'https://seedinfra.store/api'\n" +
                        "                        \n" +
                        "                        // 셀프 힐링 API 호출 함수 정의\n" +
                        "                        def callSelfHealingApi = { failType ->\n" +
                        "                            def healingApiUrl = \"${apiBaseUrl}/self-cicd/resolve\"\n" +
                        "                            def queryParams = \"projectId=${params.PROJECT_ID}&personalAccessToken=${GIT_TOKEN}&failType=${failType}\"\n" +
                        "                            \n" +
                        "                            try {\n" +
                        "                                def healingResponse = sh(script: \"\"\"\n" +
                        "                                    curl -X POST \\\n" +
                        "                                    -H 'Content-Type: application/json' \\\n" +
                        "                                    -w '\\n%{http_code}' \\\n" +
                        "                                    \"${healingApiUrl}?${queryParams}\" \n" +
                        "                                \"\"\", returnStdout: true).trim()\n" +
                        "                                \n" +
                        "                                echo \"셀프 힐링 API 호출 결과 (${failType}): ${healingResponse}\"\n" +
                        "                                env.SELF_HEALING_APPLIED = 'true'\n" +
                        "                            } catch (Exception e) {\n" +
                        "                                echo \"셀프 힐링 API 호출 실패 (${failType}): ${e.message}\"\n" +
                        "                            }\n" +
                        "                        }\n" +
                        "                        \n" +
                        "                        // 셀프 힐링 API 호출 조건 확인\n" +
                        "                        if (params.BRANCH_NAME == params.ORIGINAL_BRANCH_NAME && currentBuild.number > 1) {\n" +
                        "                            // 빌드 상태 변수 확인 (안전하게 처리)\n" +
                        "                            def frontendFailed = (env.FRONTEND_BUILD_STATUS == 'FAILED')\n" +
                        "                            def backendFailed = (env.BACKEND_BUILD_STATUS == 'FAILED')\n" +
                        "                            def healthCheckFailed = (env.HEALTH_CHECK_STATUS == 'FAILED')\n" +
                        "                            \n" +
                        "                            // 변경되지 않아 실행되지 않은 경우 처리\n" +
                        "                            if (env.FRONTEND_CHANGED == 'false') {\n" +
                        "                                frontendFailed = false\n" +
                        "                                echo \"ℹ️ 프론트엔드는 변경되지 않아 빌드가 실행되지 않았습니다.\"\n" +
                        "                            }\n" +
                        "                            if (env.BACKEND_CHANGED == 'false') {\n" +
                        "                                backendFailed = false\n" +
                        "                                echo \"ℹ️ 백엔드는 변경되지 않아 빌드가 실행되지 않았습니다.\"\n" +
                        "                            }\n" +
                        "                            \n" +
                        "                            echo \"📊 빌드 상태 요약:\\n- 프론트엔드: ${frontendFailed ? '❌ 실패' : '✅ 성공'}\\n- 백엔드: ${backendFailed ? '❌ 실패' : '✅ 성공'}\\n- 헬스 체크: ${healthCheckFailed ? '❌ 실패' : '✅ 성공'}\"\n" +
                        "                            \n" +
                        "                            // 케이스 1: 프론트엔드 빌드 실패, 백엔드 빌드 성공, 헬스 체크 성공\n" +
                        "                            if (frontendFailed && !backendFailed && !healthCheckFailed) {\n" +
                        "                                echo \"🛠️ 케이스 1: 프론트엔드 빌드 실패 → 셀프 힐링 API 호출 (BUILD)\"\n" +
                        "                                callSelfHealingApi('BUILD')\n" +
                        "                            }\n" +
                        "                            // 케이스 2: 프론트엔드 빌드 실패, 백엔드 빌드 성공, 헬스 체크 실패\n" +
                        "                            else if (frontendFailed && !backendFailed && healthCheckFailed) {\n" +
                        "                                echo \"🛠️ 케이스 2: 프론트엔드 빌드 실패 및 헬스 체크 실패 → 셀프 힐링 API 호출 (RUNTIME)\"\n" +
                        "                                callSelfHealingApi('RUNTIME')\n" +
                        "                            }\n" +
                        "                            // 케이스 3: 프론트엔드 빌드 성공, 백엔드 빌드 성공, 헬스 체크 성공\n" +
                        "                            else if (!frontendFailed && !backendFailed && !healthCheckFailed) {\n" +
                        "                                echo \"✅ 케이스 3: 모든 빌드 및 헬스 체크 성공 → 셀프 힐링 필요 없음\"\n" +
                        "                            }\n" +
                        "                            // 케이스 4: 프론트엔드 빌드 성공, 백엔드 빌드 성공, 헬스 체크 실패\n" +
                        "                            else if (!frontendFailed && !backendFailed && healthCheckFailed) {\n" +
                        "                                echo \"🛠️ 케이스 4: 헬스 체크 실패 → 셀프 힐링 API 호출 (RUNTIME)\"\n" +
                        "                                callSelfHealingApi('RUNTIME')\n" +
                        "                            }\n" +
                        "                            // 추가 케이스: 백엔드 빌드 실패\n" +
                        "                            else if (backendFailed) {\n" +
                        "                                echo \"🛠️ 추가 케이스: 백엔드 빌드 실패 → 셀프 힐링 API 호출 (BUILD)\"\n" +
                        "                                callSelfHealingApi('BUILD')\n" +
                        "                            }\n" +
                        "                            // 예상치 못한 케이스\n" +
                        "                            else {\n" +
                        "                                echo \"⚠️ 예상치 못한 상태: 빌드 상태 ${buildStatus}\\n- 정확한 진단을 위해 Jenkins 로그를 확인하세요.\"\n" +
                        "                                if (buildStatus != 'SUCCESS') {\n" +
                        "                                    echo \"❌ 빌드 실패 (기타 케이스) → 셀프 힐링 API 호출 (BUILD)\"\n" +
                        "                                    callSelfHealingApi('BUILD')\n" +
                        "                                }\n" +
                        "                            }\n" +
                        "                        } else {\n" +
                        "                            echo \"💬 원본 브랜치와 다른 브랜치 빌드 또는 첫 빌드 → 셀프 힐링 건너뜀\"\n" +
                        "                        }\n" +
                        "                        \n" +
                        "                        // 모든 작업이 완료된 후 마지막으로 빌드 로그 API 호출 (성공/실패 여부 무관)\n" +
                        "                        echo \"📝 최종 빌드 결과 로깅 API 호출 중: 프로젝트 ID ${params.PROJECT_ID}\"\n" +
                        "                        \n" +
                        "                        // 빌드 로그 API 엔드포인트 구성\n" +
                        "                        def logApiUrl = \"${apiBaseUrl}/jenkins/${params.PROJECT_ID}/log-last-build\"\n" +
                        "                        \n" +
                        "                        // 빌드 로그 API 호출 (POST 요청, 빈 본문)\n" +
                        "                        try {\n" +
                        "                            def logResponse = sh(script: \"\"\"\n" +
                        "                                curl -X POST \\\n" +
                        "                                -H 'Content-Type: application/json' \\\n" +
                        "                                -w '\\n%{http_code}' \\\n" +
                        "                                ${logApiUrl}\n" +
                        "                            \"\"\", returnStdout: true).trim()\n" +
                        "                            \n" +
                        "                            echo \"빌드 로그 API 호출 결과: ${logResponse}\"\n" +
                        "                        } catch (Exception e) {\n" +
                        "                            echo \"빌드 로그 API 호출 실패: ${e.message}\"\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                } else {\n" +
                        "                    echo \"PROJECT_ID 파라미터가 비어있어 API를 호출하지 않습니다.\"\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n" +
                        "EOF\n";

        List<String> cmds = List.of(
                "cd /var/lib/jenkins/jobs/auto-created-deployment-job &&" +  "sudo git clone " + repositoryUrl + "&& cd " + projectName,
                "sudo chmod -R 777 /var/lib/jenkins/jobs",
                jenkinsfileContent,
                "cd " + projectPath + "&& sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "&& sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "&& sudo git add Jenkinsfile",
                "cd " + projectPath + "&& sudo git commit --allow-empty -m 'add Jenkinsfile for CI/CD with SEED'",
                "cd " + projectPath + "&& sudo git push origin " + gitlabTargetBranchName
        );

        log.info("12. Jenkinsfile 생성");
        execCommands(sshSession, cmds);
    }

    // 13. Frontend Dockerfile 생성
    public void createDockerfileForFrontend(Session sshSession, String projectPath, String gitlabTargetBranchName, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.CREATE_FRONTEND_DOCKERFILE);

        String frontendDockerfileContent;

        switch (project.getFrontendFramework()) {
            case "Vue.js":
                frontendDockerfileContent =
                        "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && cat <<EOF | sudo tee Dockerfile > /dev/null\n" +
                                "FROM node:22-alpine\n" +
                                "WORKDIR /app\n" +
                                "COPY . .\n" +
                                "RUN npm install && npm run build && npm install -g serve\n" +
                                "EXPOSE 3000\n" +
                                "CMD [\"serve\", \"-s\", \"dist\"]\n" +
                                "EOF\n";
                break;

            case "React":
                frontendDockerfileContent =
                        "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && cat <<EOF | sudo tee Dockerfile > /dev/null\n" +
                                "FROM node:22-alpine\n" +
                                "WORKDIR /app\n" +
                                "COPY . .\n" +
                                "RUN npm install && npm run build && npm install -g serve\n" +
                                "EXPOSE 3000\n" +
                                "CMD [\"serve\", \"-s\", \"build\"]\n" +
                                "EOF\n";
                break;


            case "Next.js":
            default:
                frontendDockerfileContent =
                        "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && cat <<EOF | sudo tee Dockerfile > /dev/null\n" +
                                "FROM node:22-alpine AS builder\n" +
                                "WORKDIR /app\n" +
                                "COPY . .\n" +
                                "RUN npm install\n" +
                                "RUN npm run build\n" +
                                "\n" +
                                "FROM node:22-alpine\n" +
                                "WORKDIR /app\n" +
                                "COPY --from=builder /app ./\n" +
                                "EXPOSE 3000\n" +
                                "CMD [\"npm\", \"run\", \"start\"]\n" +
                                "EOF\n";
                break;
        }

        List<String> cmds = List.of(
                "cd " + projectPath + "/" + project.getFrontendDirectoryName(),
                frontendDockerfileContent,
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git add Dockerfile",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git commit --allow-empty -m 'add Dockerfile for Backend with SEED'",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git push origin " + gitlabTargetBranchName
        );

        log.info("13. Frontend Dockerfile 생성");
        execCommands(sshSession, cmds);
    }

    // 7. Gitlab Webhook 생성
    public void createGitlabWebhook(Session sshSession, Project project, String gitlabPersonalAccessToken, Long projectId, String jobName, String serverIp, String gitlabTargetBranchName) {
        serverStatusService.updateStatus(project, ServerStatus.CREATE_WEBHOOK);

        String hookUrl = "http://" + serverIp + ":9090/project/" + jobName;

        gitlabService.createPushWebhook(gitlabPersonalAccessToken, projectId, hookUrl, gitlabTargetBranchName);

        log.info("7. Gitlab Webhook 생성");
    }

    // 14. Backend Dockerfile 생성
    public void createDockerfileForBackend(Session sshSession, String projectPath, String gitlabTargetBranchName, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.CREATE_BACKEND_DOCKERFILE);

        String backendDockerfileContent;

        switch (project.getJdkBuildTool()) {
            case "Gradle":
                backendDockerfileContent =
                        "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && cat <<EOF | sudo tee Dockerfile > /dev/null\n" +
                                "# 1단계: 빌드 스테이지\n" +
                                "FROM gradle:8.5-jdk" + project.getJdkVersion() + " AS builder\n" +
                                "WORKDIR /app\n" +
                                "COPY . .\n" +
                                "RUN gradle bootJar --no-daemon\n" +
                                "\n" +
                                "# 2단계: 실행 스테이지\n" +
                                "FROM openjdk:" + project.getJdkVersion()  + "-jdk\n" +
                                "WORKDIR /app\n" +
                                "COPY --from=builder /app/build/libs/*.jar app.jar\n" +
                                "CMD [\"java\", \"-jar\", \"app.jar\"]\n" +
                                "EOF\n";
                break;

            case "Maven":
            default:
                backendDockerfileContent =
                        "cd " + projectPath+ "/" + project.getBackendDirectoryName() + " && cat <<EOF | sudo tee Dockerfile > /dev/null\n" +
                                "# 1단계: 빌드 스테이지\n" +
                                "FROM maven:3.9.6-eclipse-temurin-" + project.getJdkVersion() + " AS builder\n" +
                                "WORKDIR /app\n" +
                                "COPY . .\n" +
                                "RUN mvn clean package -B -q -DskipTests\n" +
                                "\n" +
                                "# 2단계: 실행 스테이지\n" +
                                "FROM openjdk:" + project.getJdkVersion() + "-jdk\n" +
                                "WORKDIR /app\n" +
                                "COPY --from=builder /app/target/*.jar app.jar\n" +
                                "CMD [\"java\", \"-jar\", \"app.jar\"]\n" +
                                "EOF\n";
        }

        List<String> cmds = List.of(
                "cd " + projectPath + "/" + project.getBackendDirectoryName(),
                backendDockerfileContent,
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git add Dockerfile",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git commit --allow-empty -m 'add Dockerfile for Backend with SEED'",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git push origin " + gitlabTargetBranchName
        );

        log.info("14. Backend Dockerfile 생성");
        execCommands(sshSession, cmds);
    }

    @Transactional
    public void issueAndSaveToken(Long projectId, String serverIp, Session session) {
        try {
            String jenkinsUrl = "http://" + serverIp + ":9090";
            String jenkinsJobName = "auto-created-deployment-job";
            String jenkinsUsername = "admin";

            String jenkinsToken = generateTokenViaFile(session);

            Optional<JenkinsInfo> optionalInfo = jenkinsInfoRepository.findByProjectId(projectId);

            JenkinsInfo jenkinsInfo = optionalInfo
                    .map(existing -> existing.toBuilder()
                            .baseUrl(jenkinsUrl)
                            .username(jenkinsUsername)
                            .apiToken(jenkinsToken)
                            .jobName(jenkinsJobName)
                            .build())
                    .orElseGet(() -> JenkinsInfo.builder()
                            .projectId(projectId)
                            .baseUrl(jenkinsUrl)
                            .username(jenkinsUsername)
                            .apiToken(jenkinsToken)
                            .jobName(jenkinsJobName)
                            .build());

            jenkinsInfoRepository.save(jenkinsInfo);
            log.info("✅ Jenkins API 토큰을 {}로 저장 완료", optionalInfo.isPresent() ? "업데이트" : "신규 생성");

        } catch (Exception e) {
            log.error("❌ Jenkins 토큰 파싱 또는 저장 실패", e);
            throw new BusinessException(ErrorCode.JENKINS_TOKEN_SAVE_FAILED);
        }
    }

    public String generateTokenViaFile(Session session) {
        try {
            String cmd = "sudo cat /tmp/jenkins_token";
            log.info("📤 실행 명령어: {}", cmd);

            String result = execCommand(session, cmd);
            log.info("📥 Jenkins 토큰 파일 내용:\n{}", result);

            if (result.isBlank()) {
                throw new BusinessException(ErrorCode.JENKINS_TOKEN_RESPONSE_INVALID);
            }

            return result.trim();

        } catch (Exception e) {
            log.error("❌ Jenkins 토큰 파일 파싱 실패", e);
            throw new BusinessException(ErrorCode.JENKINS_TOKEN_REQUEST_FAILED);
        }
    }

    @Override
    public void convertHttpToHttps(HttpsConvertRequest request, MultipartFile pemFile, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!userProjectRepository.existsByProjectIdAndUserId(project.getId(), user.getId())) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }

        String host = project.getServerIP();
        Session sshSession = null;

        try {
            // 1) 원격 서버 세션 등록
            log.info("세션 생성 시작");
            sshSession = createSessionWithPem(pemFile.getBytes(), host);
            log.info("세션 생성 성공");

            // 2) 명령어 실행
            convertHttpToHttpsProcess(sshSession, request);

            // 3) 성공 로그
            log.info("Https 전환을 성공했습니다.");
            notificationService.notifyProjectStatusForUsers(
                    request.getProjectId(),
                    NotificationMessageTemplate.HTTPS_SETUP_COMPLETED
            );

        } catch (Exception e) {
            log.error("SSH 연결 실패 (host={}): {}", host, e.getMessage());

            serverStatusService.updateStatus(project, ServerStatus.FAIL_HTTPS);

            notificationService.notifyProjectStatusForUsers(
                    request.getProjectId(),
                    NotificationMessageTemplate.HTTPS_SETUP_FAILED
            );
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        } finally {
            if (sshSession != null && !sshSession.isConnected()) {
                sshSession.disconnect();
            }
        }
    }

    public void convertHttpToHttpsProcess(Session sshSession, HttpsConvertRequest request) throws JSchException, IOException {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        installCertbot(sshSession, project);
        overwriteDefaultNginxConf(sshSession, request.getDomain(), project);
        reloadNginx(sshSession, project);
        issueSslCertificate(sshSession, request.getDomain(), request.getEmail(), project);
        overwriteNginxConf(sshSession, request.getDomain(), project);
        reloadNginx(sshSession, project);

        serverStatusService.updateStatus(project, ServerStatus.FINISH_CONVERT_HTTPS);
        serverStatusService.saveDomiaName(project, request.getDomain());
    }

    public void installCertbot(Session sshSession, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_CERTBOT);

        List<String> cmds = List.of(
                "sudo apt update",
                waitForAptLock(),
                "sudo apt install -y certbot python3-certbot-nginx",
                waitForAptLock()
        );

        log.info("1. Certbot 설치");
        execCommands(sshSession, cmds, "Certbot 설치", project);
    }

    public void overwriteDefaultNginxConf(Session sshSession, String domain, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.CREATE_NGINX_CONFIGURATION_FILE);

        String conf = generateDomainDefaultNginxConf(domain).replace("'", "'\"'\"'");
        String cmd = String.format("echo '%s' | sudo tee %s > /dev/null", conf, NGINX_CONF_PATH);

        List<String> cmds = List.of(cmd);

        log.info("2. Nginx Configuration File 수정");
        execCommands(sshSession, cmds, "Nginx Configuration File 수정", project);
    }

    public void reloadNginx(Session sshSession, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.RELOAD_NGINX);

        List<String> cmds = List.of(
                "sudo systemctl reload nginx"
        );

        log.info("3. Nginx 재시작");
        execCommands(sshSession, cmds, "Nginx 재시작", project);
    }

    public void issueSslCertificate(Session sshSession, String domain, String email, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.ISSUE_SSL_CERTIFICATE);

        List<String> cmds = List.of(
                String.format("sudo certbot --nginx -d %s --email %s --agree-tos --redirect --non-interactive", domain, email)
        );

        log.info("4. SSL 인증서 발급");
        execCommands(sshSession, cmds, "SSL 인증서 발급", project);
    }

    public void overwriteNginxConf(Session sshSession, String domain, Project project) throws JSchException, IOException {
        serverStatusService.updateStatus(project, ServerStatus.EDIT_NGINX_CONFIGURATION_FILE);

        String conf = generateNginxConf(domain).replace("'", "'\"'\"'");
        String cmd = String.format("echo '%s' | sudo tee %s > /dev/null", conf, NGINX_CONF_PATH);

        List<String> cmds = List.of(cmd);

        log.info("5. Nginx Configuration File 수정");
        execCommands(sshSession, cmds, "Nginx Configuration File 수정", project);
    }

    public String generateDomainDefaultNginxConf(String domain) {
        return String.format("""
            server {
                listen 80;
                server_name %s;
        
                location / {
                    proxy_pass http://localhost:3000;
                    proxy_http_version 1.1;
                    proxy_set_header Upgrade $http_upgrade;
                    proxy_set_header Connection 'upgrade';
                    proxy_set_header Host $host;
                    proxy_cache_bypass $http_upgrade;
                }
        
                location /api/ {
                    proxy_pass http://localhost:8080/api/;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    proxy_set_header X-Forwarded-Proto $scheme;
                }
        
                location /swagger-ui/ {
                    proxy_pass http://localhost:8080/swagger-ui/;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                }
        
                location /v3/api-docs {
                    proxy_pass http://localhost:8080/v3/api-docs;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    add_header Access-Control-Allow-Origin *;
                }
        
                location /ws {
                    proxy_pass http://localhost:8080/ws;
                    proxy_http_version 1.1;
                    proxy_set_header Upgrade $http_upgrade;
                    proxy_set_header Connection "upgrade";
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    proxy_read_timeout 86400;
                }
            }
            """, domain);
    }

    public String generateNginxConf(String domain) {
        return String.format("""
            server {
                listen 80;
                server_name %s;
                return 301 https://$host$request_uri;
            }

            server {
                listen 443 ssl http2;
                server_name %s;

                ssl_certificate /etc/letsencrypt/live/%s/fullchain.pem;
                ssl_certificate_key /etc/letsencrypt/live/%s/privkey.pem;
                include /etc/letsencrypt/options-ssl-nginx.conf;
                ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

                location / {
                    proxy_pass http://localhost:3000;
                    proxy_http_version 1.1;
                    proxy_set_header Upgrade $http_upgrade;
                    proxy_set_header Connection 'upgrade';
                    proxy_set_header Host $host;
                    proxy_cache_bypass $http_upgrade;
                }

                location /api/ {
                    proxy_pass http://localhost:8080/api/;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    proxy_set_header X-Forwarded-Proto $scheme;
                }

                location /swagger-ui/ {
                    proxy_pass http://localhost:8080/swagger-ui/;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                }

                location /v3/api-docs {
                    proxy_pass http://localhost:8080/v3/api-docs;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    add_header Access-Control-Allow-Origin *;
                }

                location /ws {
                    proxy_pass http://localhost:8080/ws;
                    proxy_http_version 1.1;
                    proxy_set_header Upgrade $http_upgrade;
                    proxy_set_header Connection "upgrade";
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    proxy_read_timeout 86400;
                }
            }
        """, domain, domain, domain, domain);
    }

    // Https 로그 저장
    public void saveLog(Long projectId, String stepName, String logContent, String status) {
        httpsLogRepository.save(HttpsLog.builder()
                .projectId(projectId)
                .stepName(stepName)
                .logContent(logContent)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build());
    }

    // SSH 세션 연결
    private Session createSessionWithPem(byte[] pemFile, String host) throws JSchException, IOException {
        JSch jsch = new JSch();
        jsch.addIdentity("ec2-key", pemFile, null, null);

        Session session = jsch.getSession("ubuntu", host, 22);
        Properties cfg = new Properties();
        cfg.put("StrictHostKeyChecking", "no");
        session.setConfig(cfg);
        session.connect(10000);
        log.info("SSH 연결 성공: {}", host);

        return session;
    }

    // 안전한 패키지 설치를 위한 apt lock 대기
//    private static String waitForAptLock() {
//        return String.join("\n",
//                "count=0",
//                "while sudo fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1; do",
//                "  echo \"Waiting for apt lock (sleep 5s)...\"",
//                "  sleep 5",
//                "  count=$((count+1))",
//                "  [ \"$count\" -gt 12 ] && { echo \"APT lock held too long\"; exit 1; }",
//                "done"
//        );
//    }

    private static String waitForAptLock() {
        return String.join("\n",
                "# 함수 정의: APT 잠금 확인",
                "check_apt_lock() {",
                "  if sudo lsof /var/lib/dpkg/lock-frontend > /dev/null 2>&1; then",
                "    PID=$(sudo lsof /var/lib/dpkg/lock-frontend | awk 'NR==2 {print $2}')",
                "    echo \"APT 잠금 감지: 프로세스 $PID가 잠금 중\"",
                "    PS_INFO=$(ps -p $PID -o comm=,args= 2>/dev/null || echo \"프로세스 정보 없음\")",
                "    echo \"프로세스 정보: $PS_INFO\"",
                "    return 0",
                "  else",
                "    return 1",
                "  fi",
                "}",
                "",
                "# 함수 정의: 안전하게 잠금 제거 시도",
                "try_safe_unlock() {",
                "  echo \"안전하게 APT 잠금 제거 시도...\"",
                "  # dpkg 설정 복구 시도",
                "  sudo dpkg --configure -a || true",
                "  # 다양한 잠금 파일 확인",
                "  for lockfile in /var/lib/dpkg/lock /var/lib/dpkg/lock-frontend /var/lib/apt/lists/lock /var/cache/apt/archives/lock; do",
                "    if [ -f \"$lockfile\" ]; then",
                "      echo \"잠금 파일 확인: $lockfile\"",
                "      if sudo lsof $lockfile >/dev/null 2>&1; then",
                "        echo \"$lockfile 사용 중, 건너뜀\"",
                "      else",
                "        echo \"$lockfile 사용되지 않음, 제거 중...\"",
                "        sudo rm -f $lockfile",
                "      fi",
                "    fi",
                "  done",
                "  return 0",
                "}",
                "",
                "# 메인 로직: 잠금 대기 및 처리",
                "count=0",
                "wait_count=0",
                "max_wait=36  # 3분 (5초 * 36)",
                "fixed=false",
                "",
                "while check_apt_lock; do",
                "  wait_count=$((wait_count+1))",
                "  if [ \"$wait_count\" -le \"$max_wait\" ]; then",
                "    echo \"APT 잠금 대기 중... ($wait_count/$max_wait, 5초 대기)\"",
                "    sleep 5",
                "  else",
                "    # 최대 대기 시간 초과, 안전 해제 시도",
                "    echo \"최대 대기 시간 초과 (${max_wait}회), 안전 해제 시도...\"",
                "    try_safe_unlock",
                "    count=$((count+1))",
                "    wait_count=0",
                "",
                "    # 최대 3번 시도",
                "    if [ \"$count\" -gt 3 ]; then",
                "      echo \"[심각] 여러 번 시도해도 APT 잠금 해제 실패\"",
                "      # 실행 중인 APT 프로세스 정보 출력",
                "      echo \"현재 실행 중인 APT 관련 프로세스:\"",
                "      ps aux | grep -E 'apt|dpkg' | grep -v grep",
                "      exit 1",
                "    fi",
                "  fi",
                "done",
                "",
                "# 최종 확인 및 설정 복구",
                "sudo dpkg --configure -a || true",
                "echo \"APT 잠금 확인 완료, 계속 진행합니다.\""
        );
    }

    // 스크립트 실행
    private String execCommand(Session session, String command) throws JSchException, IOException {
        ChannelExec channel = null;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        try {
            // 1) 채널 오픈 & 명령 설정
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setOutputStream(stdout);
            channel.setErrStream(stderr);

            // 2) 채널 연결 타임아웃 (예: 60초)
            channel.connect(60000);

            // 3) 명령 실행 대기 (예: 10분)
            long start = System.currentTimeMillis();
            long maxWait = 10 * 60_000;
            while (!channel.isClosed()) {
                if (System.currentTimeMillis() - start > maxWait) {
                    channel.disconnect();
                    throw new IOException("명령 실행 타임아웃: " + command);
                }
                Thread.sleep(1000);
            }

            // 4) 종료 코드 확인
            int code = channel.getExitStatus();
            if (code != 0) {
                String stdErrMsg = stderr.toString(StandardCharsets.UTF_8);
                String stdOutMsg = stdout.toString(StandardCharsets.UTF_8);
                throw new IOException(String.format(
                        "명령 실패(exit=%d)\n[STDERR]\n%s\n[STDOUT]\n%s", code, stdErrMsg, stdOutMsg
                ));
            }

            // 5) 정상 출력 반환
            return stdout.toString(StandardCharsets.UTF_8);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("명령 대기 중 인터럽트", e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private void execCommands(Session sshSession, List<String> cmds) throws JSchException, IOException {
        for (String cmd : cmds) {
            log.info("명령 수행:\n{}", cmd);
            String output = execCommandWithLiveOutput(sshSession, cmd, 15 * 60 * 1000);
            log.info("명령 결과:\n{}", output);
        }
    }

    private void execCommands(Session sshSession, List<String> cmds, String stepName, Project project) throws JSchException, IOException {
        StringBuilder outputBuilder = new StringBuilder();
        String status = "SUCCESS";

        try {
            for (String cmd : cmds) {
                log.info("명령 수행:\n{}", cmd);
                String output = execCommandWithLiveOutput(sshSession, cmd, 15 * 60 * 1000);
                outputBuilder.append(output).append("\n");
                log.info("명령 결과:\n{}", output);
            }
            // 이 단계의 모든 명령어가 성공적으로 완료된 후 성공 로그 저장
            saveLog(project.getId(), stepName, outputBuilder.toString(), status);
        } catch (Exception e) {
            // 실패 로그 저장
            status = "FAIL";
            String errorMsg = e.getMessage();
            log.error("명령 실패: {}", errorMsg);
            saveLog(project.getId(), stepName, errorMsg, status);

            throw e;
        }
    }
}
