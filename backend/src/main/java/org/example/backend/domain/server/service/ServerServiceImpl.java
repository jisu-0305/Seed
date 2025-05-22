package org.example.backend.domain.server.service;

import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.common.util.ServerAutoDeploymentFileManagerUtil;
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
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerServiceImpl implements ServerService {

    // Services
    private final GitlabService gitlabService;
    private final ServerStatusService serverStatusService;
    private final RedisSessionManager redisSessionManager;
    private final NotificationServiceImpl notificationService;

    // Repositories
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final HttpsLogRepository httpsLogRepository;
    private final UserProjectRepository userProjectRepository;
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final ApplicationRepository applicationRepository;
    private final ProjectFileRepository projectFileRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
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

        Session sshSession = null;

        try {
            // 1. SSH 세션 생성
            sshSession = createSessionWithPem(pemFile.getBytes(), project.getServerIP());

            // 2) 자동 배포 세팅 스크립트 실행
            autoDeploymentSettingProcess(sshSession, user, project, frontEnv, backEnv);

            // 3) 프로젝트 자동 배포 활성화
            serverStatusService.updateStatus(project, ServerStatus.FINISH);

            // 4) 세팅 성공 메시지 전송
            notificationService.notifyProjectStatusForUsers(projectId, NotificationMessageTemplate.EC2_SETUP_COMPLETED_SUCCESS);

            log.info("자동 배포 세팅이 성공적으로 완료되었습니다.");

        } catch (Exception e) {
            serverStatusService.updateStatus(project, ServerStatus.FAIL);
            notificationService.notifyProjectStatusForUsers(projectId, NotificationMessageTemplate.EC2_SETUP_FAILED);

            if (e instanceof BusinessException be) {
                ErrorCode errorCode = be.getErrorCode();
                log.error("비즈니스 예외 발생, 코드: {}", errorCode.getCode());
                throw new BusinessException(errorCode);
            }

        } finally {
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
            }
        }
    }

    public void autoDeploymentSettingProcess(Session sshSession, User user, Project project, byte[] frontEnvFile, byte[] backEnvFile) throws BusinessException {
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
        createGitlabWebhook(project, user.getGitlabPersonalAccessToken(), gitlabProject.getGitlabProjectId(), "auto-created-deployment-job", project.getServerIP(), project.getGitlabTargetBranchName());
        createDockerfileForBackend(sshSession, projectPath, project.getGitlabTargetBranchName(), project);
        createAndSaveJenkinsToken(sshSession, project);
    }

    // 1. 스왑 메모리 설정
    public void setSwapMemory(Session sshSession, Project project) throws BusinessException {
        log.info("1. Swap Memory");
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

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SET_SWAP_MEMORY_FAILED);
        }
    }

    // 2. 패키지 업데이트
    public void updatePackageManager(Session sshSession, Project project) throws BusinessException {
        log.info("2. 패키지 업데이트");
        serverStatusService.updateStatus(project, ServerStatus.UPDATE_PACKAGE);

        List<String> cmds = List.of(
                "sudo timedatectl set-timezone Asia/Seoul",
                "sudo apt update",
                "sudo apt upgrade -y"
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UPDATE_PACKAGE_MANAGER_FAILED);
        }
    }

    // 3. JDK 설치 (for Jenkins)
    public void installJDK(Session sshSession, Project project) throws BusinessException {
        log.info("3. JDK 설치");
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_JDK);

        List<String> cmds = List.of(
                "sudo apt install -y openjdk-17-jdk",
                "java -version"
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INSTALL_JDK_FAILED);
        }
    }

    // 4. Docker 설치
    public void installDocker(Session sshSession, Project project) throws BusinessException {
        log.info("4. Docker 설치");
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_DOCKER);

        List<String> cmds = List.of(
                // 5-1. 공식 GPG 키 추가
                "sudo apt install -y ca-certificates curl gnupg",
                "sudo install -m 0755 -d /etc/apt/keyrings",
                "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --batch --yes --no-tty --dearmor -o /etc/apt/keyrings/docker.gpg",

                // 5-2. Docker 레포지토리 등록
                "echo \\\n" +
                        "  \"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \\\n" +
                        "  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo \\\"$VERSION_CODENAME\\\") stable\" | \\\n" +
                        "  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null",

                // 5-3. Docker 설치
                "sudo apt update",
                "sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin",

                // 5-6. systemd 오버라이드 파일 생성
                "sudo mkdir -p /etc/systemd/system/docker.service.d",

                """
                sudo tee /etc/systemd/system/docker.service.d/override.conf > /dev/null << 'EOF'
                [Service]
                ExecStart=
                ExecStart=/usr/bin/dockerd \\
                  -H fd:// \\
                  -H unix:///var/run/docker.sock \\
                  -H tcp://0.0.0.0:3789 \\
                  --containerd=/run/containerd/containerd.sock
                EOF
                """.stripIndent(),

                // 5-7. Docker 서비스 재시작
                "sudo systemctl daemon-reload",
                "sudo systemctl enable docker",
                "sudo systemctl restart docker",
                "sudo docker network create mynet || true"
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INSTALL_DOCKER_FAILED);
        }
    }

    // 5. 사용자 지정 어플리케이션 실행
    public void runApplicationList(Session sshSession, Project project, byte[] backendEnvFile) throws BusinessException {
        log.info("5. 사용자 지정 어플리케이션 실행");
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
                                }
                            }
                        }

                        // 마지막에 이미지:태그
                        runSb.append(image).append(":").append(defaultTag);

                        String run = runSb.toString();

                        return Stream.of(stop, rm, run);
                    })
                    .toList();

            execCommands(sshSession, cmds);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.RUN_APPLICATIONS_FAILED);
        }
    }

    // 6. Nginx 설치
    public void installNginx(Session sshSession, Project project, String serverIp) throws BusinessException {
        log.info("6. Nginx 설치");
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_NGINX);

        String nginxConf = ServerAutoDeploymentFileManagerUtil.createHttpNginxConf(serverIp);

        List<String> cmds = List.of(
                // 7-1. Nginx 설치
                "sudo apt install -y nginx",
                "sudo systemctl enable nginx",
                "sudo systemctl start nginx",

                // 7-2. app.conf 생성
                "sudo tee /etc/nginx/sites-available/app.conf > /dev/null << 'EOF'\n" + nginxConf + "EOF",

                // 7-3. 심볼릭 링크 생성
                "sudo ln -sf /etc/nginx/sites-available/app.conf /etc/nginx/sites-enabled/app.conf",

                // 7-4. 기존 default 링크 제거
                "sudo rm -f /etc/nginx/sites-enabled/default",

                // 7-5. 설정 테스트 및 적용
                "sudo nginx -t",
                "sudo systemctl reload nginx"
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INSTALL_NGINX_FAILED);
        }
    }

    // 8. Jenkins 설치
    public void installJenkins(Session sshSession, Project project) throws BusinessException {
        log.info("8. Jenkins 설치");
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_JENKINS);

        List<String> cmds = List.of(
                "sudo mkdir -p /usr/share/keyrings",
                "curl -fsSL https://pkg.jenkins.io/debian/jenkins.io-2023.key | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null",
                "echo 'deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian binary/' | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null",
                "sudo apt update",
                "sudo apt install -y jenkins"
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INSTALL_JENKINS_FAILED);
        }
    }

    // 9. Jenkins 사용자 등록 / 플러그인 설치
    public void installJenkinsPlugins(Session sshSession, Project project) throws BusinessException {
        log.info("9. Jenkins 사용자 등록 및 플러그인 설치");
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
                """
                    sudo tee /var/lib/jenkins/users/admin/config.xml > /dev/null <<EOF
                    <?xml version='1.1' encoding='UTF-8'?>
                    <user>
                      <fullName>admin</fullName>
                      <properties>
                        <hudson.security.HudsonPrivateSecurityRealm_-Details>
                          <passwordHash>#jbcrypt:$2b$12$6CPsRl/Dz/hQRDDoMCyUyuk.q3QsYwnsH8cSzi/43H1ybVsn4yBva</passwordHash>
                        </hudson.security.HudsonPrivateSecurityRealm_-Details>
                      </properties>
                    </user>
                    EOF
                """.stripIndent(),

                "sudo mkdir -p /var/lib/jenkins/init.groovy.d",
                """
                    sudo tee /var/lib/jenkins/init.groovy.d/init_token.groovy > /dev/null <<EOF
                    import jenkins.model.*
                    import jenkins.security.ApiTokenProperty
                
                    def instance = Jenkins.get()
                    def user = instance.getUser("admin")
                    if (user == null) {
                        println("[INIT] Jenkins user 'admin' not found.")
                    } else {
                        def token = user.getProperty(ApiTokenProperty.class)
                                        .getTokenStore()
                                        .generateNewToken("init-token")
                        println("[INIT] Jenkins API Token: " + token.plainValue)
                        new File("/tmp/jenkins_token").text = token.plainValue
                    }
                    EOF
                """.stripIndent(),

                "sudo chown -R jenkins:jenkins /var/lib/jenkins/users",
                "sudo chown -R jenkins:jenkins /var/lib/jenkins/init.groovy.d",

                "curl -L https://github.com/jenkinsci/plugin-installation-manager-tool/releases/download/2.12.13/jenkins-plugin-manager-2.12.13.jar -o ~/jenkins-plugin-cli.jar",
                "sudo systemctl stop jenkins",

                // S3로부터 플러그인 다운로드 및 설치
                "sudo mkdir -p /var/lib/jenkins/plugins",
                "cd /tmp",
                "wget https://a609-betty-bucket.s3.ap-northeast-2.amazonaws.com/jenkins/plugins/plugins-cache.tar.gz",
                "tar xzf plugins-cache.tar.gz",
                "sudo cp *.jpi /var/lib/jenkins/plugins/",

                // Jenkins 재실행
                "sudo chown -R jenkins:jenkins /var/lib/jenkins/plugins",
                "sudo usermod -aG docker jenkins",
                "sudo systemctl daemon-reload",
                "sudo systemctl restart jenkins"
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INSTALL_JENKINS_PLUGINS_FAILED);
        }
    }

    // 10. Jenkins Configuration 설정 (PAT 등록, 환경변수 등록)
    public void setJenkinsConfiguration(Session sshSession, Project project, String gitlabUsername, String gitlabToken, byte[] frontEnvFile, byte[] backEnvFile) throws BusinessException {
        log.info("10. Jenkins Configuration 설정 (PAT 등록, 환경변수 등록)");
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

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SET_JENKINS_CONFIGURATION_FAILED);
        }
    }

    // 11. Jenkins Pipeline 생성
    public void createJenkinsPipeline(Session sshSession, Project project, String jobName, String gitRepoUrl, String credentialsId, String gitlabTargetBranchName) throws BusinessException {
        log.info("11. Jenkins Pipeline 생성");
        serverStatusService.updateStatus(project, ServerStatus.CREATE_JENKINS_PIPELINE);

        String jenkinsPipelineConfigXml = ServerAutoDeploymentFileManagerUtil.createJenkinsPipelineConfigXml(gitRepoUrl, credentialsId, gitlabTargetBranchName);

        List<String> cmds = List.of(
                jenkinsPipelineConfigXml,
                "wget http://localhost:9090/jnlpJars/jenkins-cli.jar",
                "java -jar jenkins-cli.jar -s http://localhost:9090/ -auth admin:pwd123 create-job " + jobName + " < job-config.xml"
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREATE_JENKINS_PIPELINE_FAILED);
        }
    }

    // 12. Jenkinsfile 생성
    public void createJenkinsFile(Session sshSession, String repositoryUrl, String projectPath, String projectName, String gitlabTargetBranchName, String namespace, Project project) throws BusinessException {
        log.info("12. Jenkinsfile 생성");
        serverStatusService.updateStatus(project, ServerStatus.CREATE_JENKINSFILE);

        String frontendDockerScript = ServerAutoDeploymentFileManagerUtil.createFrontendDockerScript(project.getFrontendFramework());

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

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREATE_JENKINS_FILE_FAILED);
        }
    }

    // 13. Frontend Dockerfile 생성
    public void createDockerfileForFrontend(Session sshSession, String projectPath, String gitlabTargetBranchName, Project project) throws BusinessException {
        log.info("13. Frontend Dockerfile 생성");
        serverStatusService.updateStatus(project, ServerStatus.CREATE_FRONTEND_DOCKERFILE);

        String frontendDockerfileContent = ServerAutoDeploymentFileManagerUtil.createFrontendDockerfileContent(project.getFrontendFramework(), projectPath, project.getFrontendDirectoryName());

        List<String> cmds = List.of(
                "cd " + projectPath + "/" + project.getFrontendDirectoryName(),
                frontendDockerfileContent,
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git add Dockerfile",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git commit --allow-empty -m 'add Dockerfile for Backend with SEED'",
                "cd " + projectPath + "/" + project.getFrontendDirectoryName() + " && sudo git push origin " + gitlabTargetBranchName
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREATE_FRONTEND_DOCKERFILE_FAILED);
        }
    }

    // 14. Gitlab Webhook 생성
    public void createGitlabWebhook(Project project, String gitlabPersonalAccessToken, Long projectId, String jobName, String serverIp, String gitlabTargetBranchName) throws BusinessException {
        log.info("7. Gitlab Webhook 생성");
        serverStatusService.updateStatus(project, ServerStatus.CREATE_WEBHOOK);

        String hookUrl = "http://" + serverIp + ":9090/project/" + jobName;

        try {
            gitlabService.createPushWebhook(gitlabPersonalAccessToken, projectId, hookUrl, gitlabTargetBranchName);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREATE_GITLAB_WEBHOOK_FAILED);
        }
    }

    // 15. Backend Dockerfile 생성
    public void createDockerfileForBackend(Session sshSession, String projectPath, String gitlabTargetBranchName, Project project) throws BusinessException {
        log.info("14. Backend Dockerfile 생성");
        serverStatusService.updateStatus(project, ServerStatus.CREATE_BACKEND_DOCKERFILE);

        String backendDockerfileContent = ServerAutoDeploymentFileManagerUtil.createBackendDockerfileContent(project.getJdkBuildTool(), projectPath, project.getBackendDirectoryName(), project.getJdkVersion());

        List<String> cmds = List.of(
                "cd " + projectPath + "/" + project.getBackendDirectoryName(),
                backendDockerfileContent,
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git add Dockerfile",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git commit --allow-empty -m 'add Dockerfile for Backend with SEED'",
                "cd " + projectPath + "/" + project.getBackendDirectoryName() + " && sudo git push origin " + gitlabTargetBranchName
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREATE_BACKEND_DOCKERFILE_FAILED);
        }
    }

    public void createAndSaveJenkinsToken(Session sshSession, Project project) throws BusinessException {
        String jenkinsUrl = "http://" + project.getServerIP() + ":9090";
        String jenkinsJobName = "auto-created-deployment-job";
        String jenkinsUsername = "admin";

        String jenkinsToken = generateTokenViaFile(sshSession);

        Optional<JenkinsInfo> optionalInfo = jenkinsInfoRepository.findByProjectId(project.getId());

        JenkinsInfo jenkinsInfo = optionalInfo
                .map(existing -> existing.toBuilder()
                        .baseUrl(jenkinsUrl)
                        .username(jenkinsUsername)
                        .apiToken(jenkinsToken)
                        .jobName(jenkinsJobName)
                        .build())
                .orElseGet(() -> JenkinsInfo.builder()
                        .projectId(project.getId())
                        .baseUrl(jenkinsUrl)
                        .username(jenkinsUsername)
                        .apiToken(jenkinsToken)
                        .jobName(jenkinsJobName)
                        .build());

        jenkinsInfoRepository.save(jenkinsInfo);

        List<String> cmds = List.of(
                "sudo rm -f /var/lib/jenkins/init.groovy.d/init_token.groovy",
                "sudo rm -f /tmp/jenkins_token"
        );

        try {
            execCommands(sshSession, cmds);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREATE_AND_SAVE_JENKINS_TOKEN_FAILED);
        }
    }

    public String generateTokenViaFile(Session session) throws BusinessException {
        try {
            String cmd = "sudo cat /tmp/jenkins_token";
            String result = execCommand(session, cmd);

            if (result.isBlank()) {
                throw new BusinessException(ErrorCode.JENKINS_TOKEN_RESPONSE_INVALID);
            }

            return result.trim();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREATE_AND_SAVE_JENKINS_TOKEN_FAILED);
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

        Session sshSession = null;

        try {
            // 1) 세션 연결
            sshSession = createSessionWithPem(pemFile.getBytes(), project.getServerIP());

            // 2) Https 전환 스크립트 수행
            convertHttpToHttpsProcess(sshSession, request, project);

            // 3) Https 전환 성공 알림
            notificationService.notifyProjectStatusForUsers(request.getProjectId(), NotificationMessageTemplate.HTTPS_SETUP_COMPLETED);

            log.info("HTTPS 세팅이 성공적으로 완료되었습니다.");

        } catch (Exception e) {
            serverStatusService.updateStatus(project, ServerStatus.FAIL_HTTPS);
            notificationService.notifyProjectStatusForUsers(request.getProjectId(), NotificationMessageTemplate.HTTPS_SETUP_FAILED);

        } finally {
            if (sshSession != null && !sshSession.isConnected()) {
                sshSession.disconnect();
            }
        }
    }

    public void convertHttpToHttpsProcess(Session sshSession, HttpsConvertRequest request, Project project) throws BusinessException {
        installCertbot(sshSession, project);
        overwriteDefaultNginxConf(sshSession, request.getDomain(), project);
        reloadNginx(sshSession, project);
        issueSslCertificate(sshSession, request.getDomain(), request.getEmail(), project);
        overwriteNginxConf(sshSession, request.getDomain(), project);
        reloadNginx(sshSession, project);

        serverStatusService.updateStatus(project, ServerStatus.FINISH_CONVERT_HTTPS);
        serverStatusService.saveDomiaName(project, request.getDomain());
    }

    public void installCertbot(Session sshSession, Project project) throws BusinessException {
        log.info("1. Certbot 설치");
        serverStatusService.updateStatus(project, ServerStatus.INSTALL_CERTBOT);

        List<String> cmds = List.of(
                "sudo apt update",
                "sleep 10",
                "sudo apt install -y certbot python3-certbot-nginx"
        );

        try {
            execCommands(sshSession, cmds, "Certbot 설치", project);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INSTALL_CERTBOT_FAILED);
        }
    }

    public void overwriteDefaultNginxConf(Session sshSession, String domain, Project project) throws BusinessException {
        log.info("2. Nginx Configuration File 수정");
        serverStatusService.updateStatus(project, ServerStatus.CREATE_NGINX_CONFIGURATION_FILE);

        String conf = ServerAutoDeploymentFileManagerUtil.createHttpNginxConfWithDomain(domain).replace("'", "'\"'\"'");
        String cmd = String.format("echo '%s' | sudo tee %s > /dev/null", conf, "/etc/nginx/sites-available/app.conf");

        List<String> cmds = List.of(
                cmd
        );

        try {
            execCommands(sshSession, cmds, "Nginx Configuration File 수정", project);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREATE_DEFAULT_NGINX_CONF_FAILED);
        }
    }

    public void reloadNginx(Session sshSession, Project project) throws BusinessException {
        log.info("3. Nginx 재시작");
        serverStatusService.updateStatus(project, ServerStatus.RELOAD_NGINX);

        List<String> cmds = List.of(
                "sudo systemctl reload nginx"
        );

        try {
            execCommands(sshSession, cmds, "Nginx 재시작", project);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.RELOAD_NGINX_FAILED);
        }
    }

    public void issueSslCertificate(Session sshSession, String domain, String email, Project project) throws BusinessException {
        log.info("4. SSL 인증서 발급");
        serverStatusService.updateStatus(project, ServerStatus.ISSUE_SSL_CERTIFICATE);

        List<String> cmds = List.of(
                String.format("sudo certbot --nginx -d %s --email %s --agree-tos --redirect --non-interactive", domain, email)
        );

        try {
            execCommands(sshSession, cmds, "SSL 인증서 발급", project);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.ISSUE_SSL_CERTIFICATE_FAILED);
        }
    }

    public void overwriteNginxConf(Session sshSession, String domain, Project project) throws BusinessException {
        log.info("5. Nginx Configuration File 수정");
        serverStatusService.updateStatus(project, ServerStatus.EDIT_NGINX_CONFIGURATION_FILE);

        String conf = ServerAutoDeploymentFileManagerUtil.createHttpsNginxConfWithDomain(domain).replace("'", "'\"'\"'");
        String cmd = String.format("echo '%s' | sudo tee %s > /dev/null", conf, "/etc/nginx/sites-available/app.conf");

        List<String> cmds = List.of(cmd);

        try {
            execCommands(sshSession, cmds, "Nginx Configuration File 수정", project);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EDIT_NGINX_CONF_FAILED);
        }
    }

    // SSH 세션 연결
    public Session createSessionWithPem(byte[] pemFile, String serverIp) throws BusinessException {
        try {
            log.info("SSH 연결 시도: {}", serverIp);
            JSch jsch = new JSch();
            jsch.addIdentity("ec2-key", pemFile, null, null);

            Session session = jsch.getSession("ubuntu", serverIp, 22);
            Properties cfg = new Properties();
            cfg.put("StrictHostKeyChecking", "no");
            session.setConfig(cfg);
            session.connect(10000);
            log.info("SSH 연결 성공: {}", serverIp);

            return session;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREATE_SSH_SESSION_FAILED);
        }
    }

    // 스크립트 실행
    private String execCommand(Session session, String command) throws JSchException, IOException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        try (ByteArrayOutputStream stdout = new ByteArrayOutputStream();
             ByteArrayOutputStream stderr = new ByteArrayOutputStream()) {

            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setOutputStream(stdout);
            channel.setErrStream(stderr);

            channel.connect(60_000);

            while (!channel.isClosed()) {
                if (System.currentTimeMillis() - System.currentTimeMillis() > (10 * 60000)) {
                    channel.disconnect();
                    throw new IOException("명령 실행 타임아웃: " + command);
                }
                Thread.sleep(1_000);
            }

            int code = channel.getExitStatus();
            if (code != 0) {
                String err = stderr.toString(StandardCharsets.UTF_8);
                String out = stdout.toString(StandardCharsets.UTF_8);
                throw new IOException(String.format(
                        "명령 실패(exit=%d)%n[STDERR]%n%s%n[STDOUT]%n%s", code, err, out
                ));
            }
            return stdout.toString(StandardCharsets.UTF_8);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("명령 대기 중 인터럽트", ie);
        } finally {
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private void execCommands(Session sshSession, List<String> cmds) throws Exception {
        for (String cmd : cmds) {
            log.info("명령 수행:\n{}", cmd);
            String output = execCommandWithLiveOutput(sshSession, cmd);
            log.info("명령 결과:\n{}", output);
        }
    }

    private void execCommands(Session sshSession, List<String> cmds, String stepName, Project project) {
        String status = "SUCCESS";
        StringBuilder outputBuilder = new StringBuilder();
        String errorMessage = null;

        try {
            for (String cmd : cmds) {
                log.info("명령 수행:\n{}", cmd);
                String output = execCommandWithLiveOutput(sshSession, cmd);
                outputBuilder.append(output).append("\n");
                log.info("명령 결과:\n{}", output);
            }
        } catch (InterruptedException ie) {
            // 1) 인터럽트 복원
            Thread.currentThread().interrupt();
            status = "INTERRUPTED";
            errorMessage = "명령 실행 중 인터럽트: " + ie.getMessage();
            log.error(errorMessage, ie);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);

        } catch (JSchException | IOException e) {
            status = "FAIL";
            errorMessage = e.getMessage();
            log.error("명령 실패: {}", errorMessage, e);
            throw new BusinessException(ErrorCode.AUTO_DEPLOYMENT_SETTING_FAILED);

        } catch (Exception e) {
            status = "FAIL";
            errorMessage = e.getMessage();
            log.error("예기치 못한 오류: {}", errorMessage, e);
            throw new BusinessException(ErrorCode.AUTO_DEPLOYMENT_SETTING_FAILED);

        } finally {
            String logContent = (status.equals("SUCCESS")
                    ? outputBuilder.toString()
                    : errorMessage);

            httpsLogRepository.save(HttpsLog.builder()
                    .projectId(project.getId())
                    .stepName(stepName)
                    .logContent(logContent)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    public String execCommandWithLiveOutput(Session session, String command) throws JSchException, IOException, InterruptedException {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            channel.connect(15 * 60 * 1000);

            StringBuilder output = new StringBuilder();
            byte[] buffer = new byte[1024];

            // 채널이 닫히고 남은 출력까지 모두 처리
            while (!channel.isClosed() || stdout.available() > 0 || stderr.available() > 0) {
                if (stdout.available() > 0) {
                    int len = stdout.read(buffer);
                    output.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
                }
                if (stderr.available() > 0) {
                    int len = stderr.read(buffer);
                    output.append("[ERROR] ")
                            .append(new String(buffer, 0, len, StandardCharsets.UTF_8));
                }
                Thread.sleep(100);
            }

            int exitStatus = channel.getExitStatus();
            if (exitStatus != 0) {
                throw new IOException(
                        String.format("명령 실패 (exit=%d): %s", exitStatus, command)
                );
            }
            return output.toString();
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private Map<String, String> parseEnvFile(byte[] envFileBytes) throws BusinessException{
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
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ENVIRONMENT_PARSE_FAILED);
        }

        return envMap;
    }

}
