package org.example.backend.domain.server.service;

import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.request.server.HttpsConvertRequest;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.domain.jenkins.entity.JenkinsInfo;
import org.example.backend.domain.jenkins.repository.JenkinsInfoRepository;
import org.example.backend.domain.project.entity.Application;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.entity.ProjectApplication;
import org.example.backend.domain.project.entity.ProjectFile;
import org.example.backend.domain.project.enums.FileType;
import org.example.backend.domain.project.repository.ApplicationRepository;
import org.example.backend.domain.project.repository.ProjectApplicationRepository;
import org.example.backend.domain.project.repository.ProjectFileRepository;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.domain.server.entity.HttpsLog;
import org.example.backend.domain.server.repository.HttpsLogRepository;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerServiceImpl implements ServerService {

    private final UserRepository userRepository;
    private final UserProjectRepository userProjectRepository;
    private final ProjectRepository projectRepository;
    private final RedisSessionManager redisSessionManager;
    private final GitlabService gitlabService;
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final HttpsLogRepository httpsLogRepository;
    private final ApplicationRepository applicationRepository;

    private static final String NGINX_CONF_PATH = "/etc/nginx/sites-available/app.conf";
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectFileRepository projectFileRepository;

    @Override
    public void registerDeployment(Long projectId, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        ProjectFile pemFileEntity = projectFileRepository.findByProjectIdAndFileType(projectId, FileType.PEM)
                .orElseThrow(() -> new BusinessException(ErrorCode.PEM_NOT_FOUND));
        byte[] frontEnv = projectFileRepository.findByProjectIdAndFileType(projectId, FileType.FRONTEND_ENV)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRONT_ENV_NOT_FOUND)).getData();
        byte[] backEnv = projectFileRepository.findByProjectIdAndFileType(projectId, FileType.BACKEND_ENV)
                .orElseThrow(() -> new BusinessException(ErrorCode.BACK_ENV_NOT_FOUND)).getData();

        String host = project.getServerIP();
        Session sshSession = null;

        try {
            // 1) 원격 서버 세션 등록
            log.info("세션 생성 시작");
            sshSession = createSessionWithPem(pemFileEntity.getData(), host);
            log.info("SSH 연결 성공: {}", host);

            // 2) 인프라 설정 명령 실행
            log.info("인프라 설정 명령 실행 시작");
            for (String cmd : serverInitializeCommands(user, project, frontEnv, backEnv, project.getGitlabTargetBranchName())) {
                log.info("명령 수행:\n{}", cmd);
                String output = execCommandWithLiveOutput(sshSession, cmd, 15 * 60 * 1000);
                log.info("명령 결과:\n{}", output);
            }

            // 5) 플러그인 캐시 업로드
            log.info("플러그인 캐시 업로드 시작");
            ChannelSftp sftp = (ChannelSftp) sshSession.openChannel("sftp");
            sftp.connect();

            String localCacheDir = "/home/ubuntu/jenkins-plugins-cache";
            File cacheDir = new File(localCacheDir);
            if (!cacheDir.isDirectory()) {
                throw new IOException("플러그인 캐시 디렉터리가 없습니다: " + localCacheDir);
            }

            File[] pluginFiles = cacheDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpi"));
            if (pluginFiles != null) {
                for (File plugin : pluginFiles) {
                    String fileName = plugin.getName();
                    String remotePath = "/var/lib/jenkins/plugins/" + fileName;
                    try (InputStream fis = new FileInputStream(plugin)) {
                        log.info("Uploading plugin {} → {}", plugin.getAbsolutePath(), remotePath);
                        sftp.put(fis, remotePath);
                    }
                }
            }
            sftp.disconnect();

            // 6) 권한 설정 및 Jenkins 서비스 재시작
            execCommandWithLiveOutput(sshSession,
                    "sudo chown -R jenkins:jenkins /var/lib/jenkins/plugins", 60_000);
            execCommandWithLiveOutput(sshSession,
                    "sudo systemctl daemon-reload && sudo systemctl restart jenkins", 60_000);
            log.info("플러그인 캐시 배포 완료");


            // 2) 인프라 설정 명령 실행
            log.info("인프라 설정 명령 실행 시작");
            for (String cmd : serverInitializeCommands2(user, project, frontEnv, backEnv, project.getGitlabTargetBranchName())) {
                log.info("명령 수행:\n{}", cmd);
                String output = execCommandWithLiveOutput(sshSession, cmd, 15 * 60 * 1000);
                log.info("명령 결과:\n{}", output);
            }

            // 3) 프로젝트 자동 배포 활성화
            project.enableAutoDeployment();

            // 4) Jenkins API 토큰 발급 및 스크립트 정리
            log.info("Jenkins API 토큰 발급 시작");
            issueAndSaveToken(projectId, host, sshSession);
            execCommand(sshSession, "sudo rm -f /var/lib/jenkins/init.groovy.d/init_token.groovy");
            execCommand(sshSession, "sudo rm -f /tmp/jenkins_token");
            log.info("Jenkins 토큰 발급 및 스크립트 정리 완료");

        } catch (JSchException | IOException | SftpException e) {
            log.error("배포 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        } finally {
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
            }
        }
    }

    /**
     * 실시간 출력을 모니터링하면서 명령을 실행하는 메서드
     */
    private String execCommandWithLiveOutput(Session session, String command, long timeoutMs) throws JSchException, IOException {
        ChannelExec channel = null;

        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // 표준 출력 스트림 설정
            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            StringBuilder outputBuilder = new StringBuilder();

            channel.connect(30 * 1000);

            byte[] buffer = new byte[1024];
            long startTime = System.currentTimeMillis();
            long lastOutputTime = startTime;

            while (true) {
                // 타임아웃 체크
                if (System.currentTimeMillis() - startTime > timeoutMs) {
                    log.error("명령 타임아웃: {}", command);
                    throw new IOException("명령 실행 타임아웃: " + command);
                }

                // 지정된 시간 동안 출력이 없으면 프로세스 확인
                if (System.currentTimeMillis() - lastOutputTime > 15 * 60 * 1000) {
                    log.warn("명령 실행 중 5분 동안 출력 없음, 프로세스 상태 확인: {}", command);
                    // 관련 프로세스 확인
                    ChannelExec checkChannel = (ChannelExec) session.openChannel("exec");
                    checkChannel.setCommand("ps aux | grep -E 'apt|dpkg|jenkins' | grep -v grep");
                    ByteArrayOutputStream checkOutput = new ByteArrayOutputStream();
                    checkChannel.setOutputStream(checkOutput);
                    checkChannel.connect();

                    try {
                        while (!checkChannel.isClosed()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } finally {
                        checkChannel.disconnect();
                    }

                    log.info("관련 프로세스 상태:\n{}", checkOutput.toString());
                    lastOutputTime = System.currentTimeMillis();  // 리셋
                }

                // 출력 읽기
                while (stdout.available() > 0) {
                    int i = stdout.read(buffer, 0, buffer.length);
                    if (i < 0) break;
                    String output = new String(buffer, 0, i, StandardCharsets.UTF_8);
                    outputBuilder.append(output);
                    log.debug("명령 출력: {}", output);
                    lastOutputTime = System.currentTimeMillis();
                }

                // 오류 출력 읽기
                while (stderr.available() > 0) {
                    int i = stderr.read(buffer, 0, buffer.length);
                    if (i < 0) break;
                    String error = new String(buffer, 0, i, StandardCharsets.UTF_8);
                    outputBuilder.append("[ERROR] ").append(error);
                    log.warn("명령 오류 출력: {}", error);
                    lastOutputTime = System.currentTimeMillis();
                }

                // 명령 완료 확인
                if (channel.isClosed()) {
                    int exitStatus = channel.getExitStatus();

                    // 마지막 출력 확인
                    while (stdout.available() > 0) {
                        int i = stdout.read(buffer, 0, buffer.length);
                        if (i < 0) break;
                        String output = new String(buffer, 0, i, StandardCharsets.UTF_8);
                        outputBuilder.append(output);
                    }

                    while (stderr.available() > 0) {
                        int i = stderr.read(buffer, 0, buffer.length);
                        if (i < 0) break;
                        String error = new String(buffer, 0, i, StandardCharsets.UTF_8);
                        outputBuilder.append("[ERROR] ").append(error);
                    }

                    if (exitStatus != 0 && !command.contains("|| true")) {
                        String errorMsg = "명령 실패 (exit=" + exitStatus + "): " + command + "\n" + outputBuilder.toString();
                        log.error(errorMsg);
                        throw new IOException(errorMsg);
                    }

                    break;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("명령 대기 중 인터럽트", e);
                }
            }

            return outputBuilder.toString();
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    // 서버 배포 프로세스
    private List<String> serverInitializeCommands(User user, Project project, byte[] frontEnvFile, byte[] backEnvFile, String gitlabTargetBranchName) {
        String url = project.getRepositoryUrl();
        String repositoryUrl = url.substring(0, url.length() - 4);

        GitlabProject gitlabProject = gitlabService.getProjectByUrl(user.getGitlabPersonalAccessToken(), repositoryUrl);

        String projectPath = "/var/lib/jenkins/jobs/auto-created-deployment-job/" + gitlabProject.getName();
        String namespace = user.getUserIdentifyId() + "/" + gitlabProject.getName() + ".git";
        String gitlabProjectUrlWithToken = "https://" + user.getUserIdentifyId() + ":" + user.getGitlabPersonalAccessToken() + "@lab.ssafy.com/" + namespace;

        log.info(gitlabProject.toString());

        // 어플리케이션 목록
        List<ProjectApplication> projectApplicationList = projectApplicationRepository.findAllByProjectId(project.getId());

        return Stream.of(
                setSwapMemory(),
                updatePackageManager(),
                setJDK(),
                setDocker(),
                //runApplicationList(projectApplicationList, backEnvFile)
                setNginx(project.getServerIP()),
                setJenkins(),
                setJenkinsConfigure()
        ).flatMap(Collection::stream).toList();
    }

    private List<String> serverInitializeCommands2(User user, Project project, byte[] frontEnvFile, byte[] backEnvFile, String gitlabTargetBranchName) {
        String url = project.getRepositoryUrl();
        String repositoryUrl = url.substring(0, url.length() - 4);

        GitlabProject gitlabProject = gitlabService.getProjectByUrl(user.getGitlabPersonalAccessToken(), repositoryUrl);

        String projectPath = "/var/lib/jenkins/jobs/auto-created-deployment-job/" + gitlabProject.getName();
        String namespace = user.getUserIdentifyId() + "/" + gitlabProject.getName() + ".git";
        String gitlabProjectUrlWithToken = "https://" + user.getUserIdentifyId() + ":" + user.getGitlabPersonalAccessToken() + "@lab.ssafy.com/" + namespace;

        log.info(gitlabProject.toString());

        // 어플리케이션 목록
        List<ProjectApplication> projectApplicationList = projectApplicationRepository.findAllByProjectId(project.getId());

        return Stream.of(
                makeJenkinsJob("auto-created-deployment-job", project.getRepositoryUrl(), "gitlab-token", gitlabTargetBranchName),
                setJenkinsConfiguration(user.getUserIdentifyId(), user.getGitlabPersonalAccessToken(), frontEnvFile, backEnvFile),
                makeJenkinsFile(gitlabProjectUrlWithToken, projectPath, gitlabProject.getName(), gitlabTargetBranchName, namespace, project),
                makeDockerfileForBackend(gitlabProjectUrlWithToken, projectPath, gitlabTargetBranchName, project),
                makeDockerfileForFrontend(gitlabProjectUrlWithToken, projectPath, gitlabTargetBranchName, project),
                makeGitlabWebhook(user.getGitlabPersonalAccessToken(), gitlabProject.getGitlabProjectId(), "auto-created-deployment-job", project.getServerIP(), gitlabTargetBranchName)
        ).flatMap(Collection::stream).toList();
    }

    // [optional] 방화벽 설정
    private List<String> setFirewall() {
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
    private List<String> setSwapMemory() {
        return List.of(
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
    }

    // 2. 패키지 업데이트
    private List<String> updatePackageManager() {
        return List.of(
                "sudo apt update && sudo apt upgrade -y",
                waitForAptLock(),
                "sudo timedatectl set-timezone Asia/Seoul"
        );
    }

    // 3. JDK 설치
    private List<String> setJDK() {
        return List.of(
                "sudo apt install -y openjdk-17-jdk",
                waitForAptLock(),
                "java -version"
        );
    }

    // Node.js, npm 설치 (docker로 빌드하므로 필요없어짐)
    private List<String> setNodejs() {
        return List.of(
                "curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -",
                "sudo apt-get install -y nodejs",
                "node -v",
                "npm -v"
        );
    }

    // 4. Docker 설치 (Docker-Compose 추가 가능)
    private List<String> setDocker() {
        return List.of(

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
                "sudo systemctl restart docker"
        );
    }

    // 5. 어플리케이션 목록 도커로 실행
    private List<String> runApplicationList(List<ProjectApplication> projectApplicationList, byte[] backendEnvFile) {
        try {
            Map<String, String> envMap = parseEnvFile(backendEnvFile);
            for (String key : envMap.keySet()) {
                System.out.println(key + "=" + envMap.get(key));
            }

            return projectApplicationList.stream()
                    .flatMap(app -> {
                        String image = app.getImageName();
                        int port  = app.getPort();
                        String tag   = app.getTag();

                        // stop, rm 명령
                        String stop = "sudo docker stop " + image + " || true";
                        String rm   = "sudo docker rm "   + image + " || true";

                        // run 명령 빌드
                        StringBuilder runSb = new StringBuilder();
                        runSb.append("sudo docker run -d ")
                                .append("--restart unless-stopped ")
                                .append("--name ").append(image).append(" ")
                                .append("-p ").append(port).append(":").append(port).append(" ");

                        // 환경변수 Map 순회
                        // key값은 db에서 꺼내와야함
                        // value는 .env에서 꺼내와야함
                        Application application = applicationRepository.findById(app.getApplicationId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

                        List<String> applicationEnvList = application.getEnvVariableList();

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
                        runSb.append(image).append(":").append(tag);

                        String run = runSb.toString();

                        return Stream.of(stop, rm, run);
                    })
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> parseEnvFile(byte[] envFileBytes) throws IOException {
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

    // 6. Nginx 설치 및 설정
    private List<String> setNginx(String serverIp) {
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

        return List.of(
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
    }

    // 8. Jenkins 설치
    private List<String> setJenkins() {
        return List.of(
                "sudo mkdir -p /usr/share/keyrings",
                "curl -fsSL https://pkg.jenkins.io/debian/jenkins.io-2023.key | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null",
                "echo 'deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian binary/' | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null",
                "sudo apt update",
                waitForAptLock(),
                "sudo apt install -y --allow-downgrades jenkins=2.504",
                waitForAptLock()
        );
    }

    private List<String> setJenkinsConfigure() {
        return List.of(
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

                "sudo chown -R jenkins:jenkins /var/lib/jenkins/plugins",
                "sudo usermod -aG docker jenkins",
                "sudo systemctl daemon-reload",
                "sudo systemctl restart jenkins"
        );
    }

    // 9. Jenkins credentials 생성
    private List<String> setJenkinsConfiguration(String gitlabUsername, String gitlabToken, byte[] frontEnvFile, byte[] backEnvFile) {
        String frontEnvFileStr = Base64.getEncoder().encodeToString(frontEnvFile);
        String backEnvFileStr = Base64.getEncoder().encodeToString(backEnvFile);

        log.info(gitlabToken);

        return List.of(
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
                        "  <id>front</id>\n" +
                        "  <description></description>\n" +
                        "  <fileName>.env</fileName>\n" +
                        "  <secretBytes>" + frontEnvFileStr + "</secretBytes>\n" +
                        "</org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl>\n" +
                        "EOF"
        );
    }

    private List<String> makeJenkinsJob(String jobName, String gitRepoUrl, String credentialsId, String gitlabTargetBranchName) {
        String jobConfigXml = String.join("\n",
                "sudo tee job-config.xml > /dev/null <<EOF",
                "<?xml version='1.1' encoding='UTF-8'?>",
                "<flow-definition plugin=\"workflow-job\">",
                "  <description>GitLab 연동 자동 배포</description>",
                "  <keepDependencies>false</keepDependencies>",
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
                "          <name>*/" + gitlabTargetBranchName +"</name>",
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

        return List.of(
                jobConfigXml,
                "wget http://localhost:9090/jnlpJars/jenkins-cli.jar",
                "java -jar jenkins-cli.jar -s http://localhost:9090/ -auth admin:pwd123 create-job " + jobName + " < job-config.xml"
        );
    }

    private List<String> makeJenkinsFile(String repositoryUrl, String projectPath, String projectName, String gitlabTargetBranchName, String namespace, Project project) {

        log.info(repositoryUrl);

        String frontendDockerScript;

        switch (project.getFrontendFramework()) {
            case "Vue.js":
                frontendDockerScript =
                        "                        set -e\n" +
                                "                        docker build -f Dockerfile -t vue .\n" +
                                "                        docker stop vue || true\n" +
                                "                        docker rm vue || true\n" +
                                "                        docker run -d --env-file .env --restart unless-stopped --name vue -p 3000:3000 vue\n";
                break;

            case "React":
                frontendDockerScript =
                        "                        set -e\n" +
                                "                        docker build -f Dockerfile -t react .\n" +
                                "                        docker stop react || true\n" +
                                "                        docker rm react || true\n" +
                                "                        docker run -d --env-file .env --restart unless-stopped --name react -p 3000:3000 react\n";
                break;

            case "Next.js":
            default:
                frontendDockerScript =
                        "                        set -e\n" +
                                "                        docker build -f Dockerfile -t next .\n" +
                                "                        docker stop next || true\n" +
                                "                        docker rm next || true\n" +
                                "                        docker run -d --env-file .env --restart unless-stopped --name next -p 3000:3000 next\n";
                break;
        }

        String jenkinsfileContent =
                "cd " + projectPath + " && cat <<EOF | sudo tee Jenkinsfile > /dev/null\n" +
                        "pipeline {\n" +
                        "    agent any\n" +
                        "\n" +
                        "    stages {\n" +
                        "        stage('Checkout') {\n" +
                        "            steps {\n" +
                        "                echo '1. 워크스페이스 정리 및 소스 체크아웃'\n" +
                        "                deleteDir()\n" +
                        "                withCredentials([usernamePassword(credentialsId: 'gitlab-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {\n" +
                        "                    git branch: '" + gitlabTargetBranchName + "', url: \"https://\\$GIT_USER:\\$GIT_TOKEN@lab.ssafy.com/" + namespace + "\"\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "\n" +
                        "        stage('Build Backend') {\n" +
                        "            when {\n" +
                        "                anyOf {\n" +
                        "                    // 1) 백엔드 코드 변경 감지\n" +
                        "                    changeset pattern: '" + project.getBackendDirectoryName() + "/.*', comparator: 'REGEXP'\n" +
                        "                    // 2) 전체 빌드 (변경셋 없음)\n" +
                        "                    expression { currentBuild.changeSets.size() == 0 }\n" +
                        "                }\n" +
                        "            }\n" +
                        "            steps {\n" +
                        "                echo '2. Backend 변경 감지 또는 전체 빌드, 빌드 및 배포'\n" +
                        "                withCredentials([file(credentialsId: \"backend\", variable: 'BACKEND_ENV')]) {\n" +
                        "                    sh '''\n" +
                        "                        set -e\n" +
                        "                        echo \"  - 복사: $BACKEND_ENV → ${WORKSPACE}/" + project.getBackendDirectoryName() + "/.env\"\n" +
                        "                        cp \"\\$BACKEND_ENV\" \"\\$WORKSPACE/" + project.getBackendDirectoryName() + "/.env\"\n" +
                        "                    '''\n" +
                        "                }\n" +
                        "                dir('" + project.getBackendDirectoryName() + "') {\n" +
                        "                    sh '''\n" +
                        "                        set -e\n" +
                        "                        docker build -t spring .\n" +
                        "                        docker stop spring || true\n" +
                        "                        docker rm spring || true\n" +
                        "                        docker run -d -p 8080:8080 --env-file .env --name spring spring\n" +
                        "                    '''\n" +
                        "                }\n" +
                        "                echo '[INFO] 백엔드 완료'\n" +
                        "            }\n" +
                        "        }\n" +
                        "\n" +
                        "        stage('Build Frontend') {\n" +
                        "            when {\n" +
                        "                anyOf {\n" +
                        "                    // 1) 프론트엔드 코드 변경 감지\n" +
                        "                    changeset pattern: '" + project.getFrontendDirectoryName() + "/.*', comparator: 'REGEXP'\n" +
                        "                    // 2) 전체 빌드 (변경셋 없음)\n" +
                        "                    expression { currentBuild.changeSets.size() == 0 }\n" +
                        "                }\n" +
                        "            }\n" +
                        "            steps {\n" +
                        "                echo '3. Frontend 변경 감지 또는 전체 빌드, 빌드 및 배포'\n" +
                        "                withCredentials([file(credentialsId: \"front\", variable: 'FRONT_ENV')]) {\n" +
                        "                    sh '''\n" +
                        "                        set -e\n" +
                        "                        echo \"  - 복사: $FRONT_ENV → ${WORKSPACE}/" + project.getFrontendDirectoryName() + "/.env\"\n" +
                        "                        cp \"\\$FRONT_ENV\" \"\\$WORKSPACE/" + project.getFrontendDirectoryName() + "/.env\"\n" +
                        "                    '''\n" +
                        "                }\n" +
                        "                dir('" + project.getFrontendDirectoryName() + "') {\n" +
                        "                    sh '''\n" +
                        frontendDockerScript +
                        "                    '''\n" +
                        "                }\n" +
                        "                echo '[INFO] 프론트엔드 완료'\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n" +
                        "EOF\n";

        return List.of(
                "cd /var/lib/jenkins/jobs/auto-created-deployment-job &&" +  "sudo git clone " + repositoryUrl + "&& cd " + projectName,
                "sudo chmod -R 777 /var/lib/jenkins/jobs",
                jenkinsfileContent,
                "cd " + projectPath + "&& sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "&& sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "&& sudo git add Jenkinsfile",
                "cd " + projectPath + "&& sudo git commit --allow-empty -m 'add Jenkinsfile for CI/CD with SEED'",
                "cd " + projectPath + "&& sudo git push origin " + gitlabTargetBranchName
        );
    }

    private List<String> makeDockerfileForBackend(String repositoryUrl, String projectPath, String gitlabTargetBranchName, Project project) {

        log.info(repositoryUrl);

        String backendDockerfileContent;

        switch (project.getJdkBuildTool()) {
            case "Gradle":
                backendDockerfileContent =
                        "cd " + projectPath + "/" + project.getBackendDirectoryName() + "&& cat <<EOF | sudo tee Dockerfile > /dev/null\n" +
                                "# 1단계: 빌드 스테이지\n" +
                                "FROM gradle:8.5-jdk" + project.getJdkVersion() + " AS builder\n" +
                                "WORKDIR /app\n" +
                                "COPY . .\n" +
                                "RUN gradle bootJar --no-daemon\n" +
                                "\n" +
                                "# 2단계: 실행 스테이지\n" +
                                "FROM openjdk:" + project.getJdkVersion()  + "-jdk-slim\n" +
                                "WORKDIR /app\n" +
                                "COPY --from=builder /app/build/libs/*.jar app.jar\n" +
                                "CMD [\"java\", \"-jar\", \"app.jar\"]\n" +
                                "EOF\n";
                break;

            case "Maven":
            default:
                backendDockerfileContent =
                        "cd " + projectPath+ "/" + project.getBackendDirectoryName() + "&& cat <<EOF | sudo tee Dockerfile > /dev/null\n" +
                                "# 1단계: 빌드 스테이지\n" +
                                "FROM maven:3.9.6-eclipse-temurin-" + project.getJdkVersion() + " AS builder\n" +
                                "WORKDIR /app\n" +
                                "COPY . .\n" +
                                "RUN mvn clean package -DskipTests\n" +
                                "\n" +
                                "# 2단계: 실행 스테이지\n" +
                                "FROM openjdk:" + project.getJdkVersion() + "-jdk-slim\n" +
                                "WORKDIR /app\n" +
                                "COPY --from=builder /app/target/*.jar app.jar\n" +
                                "CMD [\"java\", \"-jar\", \"app.jar\"]\n" +
                                "EOF\n";
        }

        return List.of(
                "cd " + projectPath + "/" + project.getBackendDirectoryName(),
                backendDockerfileContent,
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git add Dockerfile",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git commit --allow-empty -m 'add Dockerfile for Backend with SEED'",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git push origin " + gitlabTargetBranchName
        );
    }

    private List<String> makeDockerfileForFrontend(String repositoryUrl, String projectPath, String gitlabTargetBranchName, Project project) {
        log.info(repositoryUrl);

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

        return List.of(
                "cd " + projectPath + "/" + project.getFrontendDirectoryName(),
                frontendDockerfileContent,
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git add Dockerfile",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git commit --allow-empty -m 'add Dockerfile for Backend with SEED'",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git push origin " + gitlabTargetBranchName
        );
    }

    private List<String> makeGitlabWebhook(String gitlabPersonalAccessToken, Long projectId, String jobName, String serverIp, String gitlabTargetBranchName) {
        String hookUrl = "http://" + serverIp + ":9090/project/" + jobName;

        gitlabService.createPushWebhook(gitlabPersonalAccessToken, projectId, hookUrl, gitlabTargetBranchName);

        //최초 실행 로직 한번 필요 그래야 아래 777의미가 있음
        //return List.of("sudo chmod -R 777 /var/lib/jenkins/workspace");
        return List.of();
    }


    private void issueAndSaveToken(Long projectId, String serverIp, Session session) {
        try {
            String jenkinsUrl = "http://" + serverIp + ":9090";
            String jenkinsJobName = "auto-created-deployment-job";
            String jenkinsUsername = "admin";

            String jenkinsToken = generateTokenViaFile(session);

            JenkinsInfo jenkinsInfo = JenkinsInfo.builder()
                    .projectId(projectId)
                    .baseUrl(jenkinsUrl)
                    .username(jenkinsUsername)
                    .apiToken(jenkinsToken)
                    .jobName(jenkinsJobName)
                    .build();

            jenkinsInfoRepository.save(jenkinsInfo);
            log.info("✅ Jenkins API 토큰을 파일에서 추출해 저장 완료");

        } catch (Exception e) {
            log.error("❌ Jenkins 토큰 파싱 또는 저장 실패", e);
            throw new BusinessException(ErrorCode.JENKINS_TOKEN_SAVE_FAILED);
        }
    }

    private String generateTokenViaFile(Session session) {
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
    public void convertHttpToHttps(HttpsConvertRequest request, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        ProjectFile pemFileEntity = projectFileRepository.findByProjectIdAndFileType(project.getId(), FileType.PEM)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!userProjectRepository.existsByProjectIdAndUserId(project.getId(), user.getId())) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }

        String host = project.getServerIP();
        Session sshSession = null;


        try {
            // 1) 원격 서버 세션 등록
            log.info("세션 생성 시작");
            sshSession = createSessionWithPem(pemFileEntity.getData(), host);
            log.info("세션 생성 성공");

            // 2) 명령어 실행
            log.info("초기화 명령 실행 시작");
            for (Map.Entry<String, String> entry : convertHttpToHttpsCommands(request)) {
                String stepName = entry.getKey();
                String command = entry.getValue();
                try {
                    log.info("명령 수행:\n{}", command);
                    String output = execCommand(sshSession, command);
                    saveLog(project.getId(), stepName, output, "SUCCESS");
                    log.info("명령 결과:\n{}", output);
                } catch (Exception e) {
                    String errorMsg = e.getMessage();
                    log.error("명령 실패: {}", errorMsg);
                    saveLog(project.getId(), stepName, errorMsg, "FAIL");
                }
            }


            // 3) 성공 로그
            log.info("Https 전환을 성공했습니다.");

        } catch (JSchException e) {
            log.error("SSH 연결 실패 (host={}): {}", host, e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        } catch (IOException e) {
            log.error("PEM 파일 로드 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);

        } finally {
            if (sshSession != null && !sshSession.isConnected()) {
                sshSession.disconnect();
            }
        }
    }

    private List<Map.Entry<String, String>> convertHttpToHttpsCommands(HttpsConvertRequest request) {
        return Stream.of(
                        Map.entry("Install Certbot", installCertbot()),
                        Map.entry("Overwrite Default Nginx Conf", overwriteDomainDefaultNginxConf(request.getDomain())),
                        Map.entry("Reload Nginx (Step 1)", reloadNginx()),
                        Map.entry("Issue SSL Certificate", issueSslCertificate(request.getDomain(), request.getEmail())),
                        Map.entry("Overwrite Nginx Conf with SSL", overwriteNginxConf(request.getDomain())),
                        Map.entry("Reload Nginx (Final)", reloadNginx())
                ).flatMap(entry -> entry.getValue().stream()
                        .map(cmd -> Map.entry(entry.getKey(), cmd)))
                .toList();
    }

    private List<String> installCertbot() {
        return List.of(
                "sudo apt update",
                waitForAptLock(),
                "sudo apt install -y certbot python3-certbot-nginx",
                waitForAptLock()
        );
    }

    private List<String> issueSslCertificate(String domain, String email) {
        return List.of(
                String.format("sudo certbot --nginx -d %s --email %s --agree-tos --redirect --non-interactive", domain, email)
        );
    }

    private List<String> overwriteNginxConf(String domain) {
        String conf = generateNginxConf(domain).replace("'", "'\"'\"'");
        String cmd = String.format("echo '%s' | sudo tee %s > /dev/null", conf, NGINX_CONF_PATH);

        return List.of(
                cmd
        );
    }

    private List<String> overwriteDomainDefaultNginxConf(String domain) {
        String conf = generateDomainDefaultNginxConf(domain).replace("'", "'\"'\"'");
        String cmd = String.format("echo '%s' | sudo tee %s > /dev/null", conf, NGINX_CONF_PATH);

        return List.of(
                cmd
        );
    }

    private List<String> reloadNginx() {
        return List.of(
                "sudo systemctl reload nginx"
        );
    }

    private String generateDomainDefaultNginxConf(String domain) {
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

    private String generateNginxConf(String domain) {
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

    private void saveLog(Long projectId, String stepName, String logContent, String status) {
        httpsLogRepository.save(HttpsLog.builder()
                .projectId(projectId)
                .stepName(stepName)
                .logContent(logContent)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build());
    }

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

    // 안전한 패키지 설치를 위한 apt lock 대기
    private static String waitForAptLock() {
        return String.join("\n",
                "count=0",
                "while sudo fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1; do",
                "  echo \"Waiting for apt lock (sleep 5s)...\"",
                "  sleep 5",
                "  count=$((count+1))",
                "  [ \"$count\" -gt 12 ] && { echo \"APT lock held too long\"; exit 1; }",
                "done"
        );
    }
}
