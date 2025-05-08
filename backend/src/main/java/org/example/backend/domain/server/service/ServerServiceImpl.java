package org.example.backend.domain.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.request.server.DeploymentRegistrationRequest;
import org.example.backend.controller.request.server.InitServerRequest;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerServiceImpl implements ServerService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RedisSessionManager redisSessionManager;
    private final GitlabService gitlabService;
    private final JenkinsInfoRepository jenkinsInfoRepository;

    @Override
    public void registerDeployment(
            DeploymentRegistrationRequest request, MultipartFile pemFile, MultipartFile frontEnvFile, MultipartFile backEnvFile, String accessToken) {

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        String host = project.getServerIP();
        Session sshSession = null;

        try {
            // 1) 원격 서버 세션 등록
            log.info("세션 생성 시작");
            sshSession = createSessionWithPem(pemFile, host);
            log.info("세션 생성 성공");

            // 2) 명령어 실행
            log.info("인프라 설정 명령 실행 시작");
            for (String cmd : serverInitializeCommands(user, project, frontEnvFile, backEnvFile, request.getGitlabTargetBranchName())) {
                log.info("명령 수행:\n{}", cmd);
                String output = execCommand(sshSession, cmd);
                log.info("명령 결과:\n{}", output);
            }


            // 3) 성공 로그
            log.info("모든 인프라 설정 세팅을 완료했습니다.");

            // jenkins api token 생성 및 저장
//            issueAndSaveToken(projectId, request.getServerIp());

        } catch (JSchException e) {
            log.error("SSH 연결 실패 (host={}): {}", host, e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        } catch (IOException e) {
            log.error("PEM 파일 로드 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);

        } finally {
            if (session != null && !sshSession.isConnected()) {
                sshSession.disconnect();
            }
        }
    }

    // 서버 배포 프로세스
    private List<String> serverInitializeCommands(User user, Project project, MultipartFile frontEnvFile, MultipartFile backEnvFile, String gitlabTargetBranchName) {
        GitlabProject gitlabProject = gitlabService.getProjectByUrl(user.getGitlabPersonalAccessToken(), "https://lab.ssafy.com/potential1205/seed-test1");

        String projectPath = "/var/lib/jenkins/jobs/auto-created-deployment-job/" + gitlabProject.getName();
        String namespace = user.getUserIdentifyId() + "/" + gitlabProject.getName() + ".git";
        String gitlabProjectUrlWithToken = "https://" + user.getUserIdentifyId() + ":" + user.getGitlabPersonalAccessToken() + "@lab.ssafy.com/" + namespace;

        log.info(gitlabProject.toString());

        return Stream.of(
                //updatePackageManager(),
                //setSwapMemory(),
                //setJDK(),
                //setNodejs(),
                //setDocker(),
                //setNginx(project.getServerIP()),
                setJenkins(),
                setJenkinsConfigure(),
                makeJenkinsJob("auto-created-deployment-job", project.getRepositoryUrl(), "gitlab-token", gitlabTargetBranchName),
                setJenkinsConfiguration(user.getUserIdentifyId(), user.getGitlabPersonalAccessToken(), frontEnvFile, backEnvFile),
                makeJenkinsFile(gitlabProjectUrlWithToken, projectPath, gitlabProject.getName(), gitlabTargetBranchName),
                makeDockerfileForBackend(gitlabProjectUrlWithToken, projectPath, gitlabTargetBranchName),
                makeGitlabWebhook(user.getGitlabPersonalAccessToken(), gitlabProject.getId(), "auto-created-deployment-job", project.getServerIP(), gitlabTargetBranchName)
        ).flatMap(Collection::stream).toList();
    }

    // 1. 방화벽 설정 (optional)
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

    // 2. 스왑 메모리 설정
    private List<String> setSwapMemory() {
        return List.of(
                "sudo fallocate -l 4G /swapfile",
                "sudo chmod 600 /swapfile",
                "sudo mkswap /swapfile",
                "sudo swapon /swapfile",
                "echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab"
        );
    }

    // 3. 패키지 업데이트 (apt, apt-get)
    private List<String> updatePackageManager() {
        return List.of(
                "sudo apt update",
                "sudo apt upgrade -y",
                "sudo apt-get update"
        );
    }

    // 4. JDK 설치
    private List<String> setJDK() {
        return List.of(
                "sudo apt install -y openjdk-17-jdk",
                "java -version"
        );
    }

    // 5. Node.js, npm 설치
    private List<String> setNodejs() {
        return List.of(
                "curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -",
                "sudo apt-get install -y nodejs",
                "node -v",
                "npm -v"
        );
    }

    // 6. Docker, Docker-Compose 설치
    private List<String> setDocker() {
        return List.of(
                // 5-1. 공식 GPG 키 추가
                "sudo apt-get install -y ca-certificates curl gnupg",
                "sudo install -m 0755 -d /etc/apt/keyrings",
                "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --batch --yes --no-tty --dearmor -o /etc/apt/keyrings/docker.gpg",

                // 5-2. Docker 레포지토리 등록
                "echo \\\n" +
                        "  \"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \\\n" +
                        "  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo \"$VERSION_CODENAME\") stable\" | \\\n" +
                        "  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null",

                // 5-3. Docker, Docker-Compose 설치
                "sudo apt-get update",
                "sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",

                // 5-4. 서비스 활성화 및 시작
                "sudo systemctl enable docker",
                "sudo systemctl start docker",
                "docker --version",
                "docker compose version"
        );
    }

    // 7. Nginx 설치
    private List<String> setNginx(String serverIp) {
        String nginxConf =
                "server {\n" +
                        "    listen 80;\n" +
                        "    server_name " + serverIp + ";\n" +
                        "\n" +
                        "    root /var/www/html;\n" +
                        "    index index.html;\n" +
                        "\n" +
                        "    location / {\n" +
                        "        try_files $uri $uri/ /index.html;\n" +
                        "    }\n" +
                        "\n" +
                        "    location /api/ {\n" +
                        "        proxy_pass http://localhost:8080/api/;\n" +
                        "        proxy_set_header Host $host;\n" +
                        "        proxy_set_header X-Real-IP $remote_addr;\n" +
                        "        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
                        "        proxy_set_header X-Forwarded-Proto $scheme;\n" +
                        "    }\n" +
                        "\n" +
                        "    location /swagger-ui/ {\n" +
                        "        proxy_pass http://localhost:8080/swagger-ui/;\n" +
                        "        proxy_set_header Host $host;\n" +
                        "        proxy_set_header X-Real-IP $remote_addr;\n" +
                        "        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
                        "    }\n" +
                        "\n" +
                        "    location /v3/api-docs {\n" +
                        "        proxy_pass http://localhost:8080/v3/api-docs;\n" +
                        "        proxy_set_header Host $host;\n" +
                        "        proxy_set_header X-Real-IP $remote_addr;\n" +
                        "        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
                        "        add_header Access-Control-Allow-Origin *;\n" +
                        "    }\n" +
                        "\n" +
                        "    location /ws {\n" +
                        "        proxy_pass http://localhost:8080/ws;\n" +
                        "        proxy_http_version 1.1;\n" +
                        "        proxy_set_header Upgrade $http_upgrade;\n" +
                        "        proxy_set_header Connection \"upgrade\";\n" +
                        "        proxy_set_header Host $host;\n" +
                        "        proxy_set_header X-Real-IP $remote_addr;\n" +
                        "        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
                        "        proxy_read_timeout 86400;\n" +
                        "    }\n" +
                        "}\n";

        return List.of(
                // 7-1. Nginx 설치
                "sudo apt install -y nginx",
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
                "sudo apt install -y --allow-downgrades jenkins=2.508"
        );
    }

    private List<String> setJenkinsConfigure() {
        return List.of(
                // 기본 폴더 초기화
                "sudo rm -rf /var/lib/jenkins/*",

                // Setup Wizard 비활성화 및 포트 변경
                "sudo sed -i '/ExecStart/ c\\ExecStart=/usr/bin/java -Djava.awt.headless=true -Djenkins.install.runSetupWizard=false -jar /usr/share/java/jenkins.war --httpPort=9090 --argumentsRealm.passwd.admin=pwd123 --argumentsRealm.roles.admin=admin' /lib/systemd/system/jenkins.service",
                "sudo systemctl daemon-reload",
                "sudo systemctl start jenkins",

                // admin 사용자 등록
                "sudo mkdir -p /var/lib/jenkins/users/admin",
                "sudo tee /var/lib/jenkins/users/admin/config.xml > /dev/null <<EOF\n" +
                        "<?xml version='1.1' encoding='UTF-8'?>\n" +
                        "<user>\n" +
                        "  <fullName>admin</fullName>\n" +
                        "  <properties>\n" +
                        "    <hudson.security.HudsonPrivateSecurityRealm_-Details>\n" +
                        "      <passwordHash>#jbcrypt:$2a$10$Dow1v0zN88bGyfprxqO2ZuhT8Vlfk7q/EGp8Hznh5CZmj1mHndOFK</passwordHash>\n" +
                        "    </hudson.security.HudsonPrivateSecurityRealm_-Details>\n" +
                        "  </properties>\n" +
                        "</user>\n" +
                        "EOF" ,

                "sudo chown -R jenkins:jenkins /var/lib/jenkins/users",
                "curl -L https://github.com/jenkinsci/plugin-installation-manager-tool/releases/download/2.12.13/jenkins-plugin-manager-2.12.13.jar -o ~/jenkins-plugin-cli.jar",
                "sudo systemctl stop jenkins",

                "sudo java -jar ~/jenkins-plugin-cli.jar --war /usr/share/java/jenkins.war \\\n" +
                        "  --plugin-download-directory=/var/lib/jenkins/plugins \\\n" +
                        "  --plugins \\\n" +
                        "  gitlab-plugin \\\n" +
                        "  gitlab-api \\\n" +
                        "  git \\\n" +
                        "  workflow-aggregator \\\n" +
                        "  docker-plugin \\\n" +
                        "  docker-workflow \\\n" +
                        "  pipeline-stage-view \\\n" +
                        "  credentials \\\n" +
                        "  credentials-binding\\\n" +
                        "  configuration-as-code",

                "sudo chown -R jenkins:jenkins /var/lib/jenkins/plugins",
                "sudo usermod -aG docker jenkins",
                "sudo systemctl daemon-reload",
                "sudo systemctl restart jenkins"
        );
    }

    // 9. Jenkins credentials 생성
    private List<String> setJenkinsConfiguration(String gitlabUsername, String gitlabToken, MultipartFile frontEnvFile, MultipartFile backEnvFile) {
        try {
            String frontEnvFileStr = Base64.getEncoder().encodeToString(frontEnvFile.getBytes());
            String backEnvFileStr = Base64.getEncoder().encodeToString(backEnvFile.getBytes());

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

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "환경변수 파일 인코딩 실패: " + e.getMessage());
        }
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

    private List<String> makeJenkinsFile(String repositoryUrl, String projectPath, String projectName, String gitlabTargetBranchName) {

        log.info(repositoryUrl);

        String jenkinsfileContent =
                "cd " + projectPath + " && cat <<EOF | sudo tee Jenkinsfile > /dev/null\n" +
                        "pipeline {\n" +
                        "    agent any\n" +
                        "\n" +
                        "    environment {\n" +
                        "        GIT_BRANCH = '" + gitlabTargetBranchName + "'\n" +
                        "        REPO_URL   = '" + repositoryUrl + "'\n" +
                        "    }\n" +
                        "\n" +
                        "    stages {\n" +
                        "        stage('Checkout') {\n" +
                        "            steps {\n" +
                        "                echo '1. 워크스페이스 정리 및 소스 체크아웃'\n" +
                        "                deleteDir()\n" +
                        "                git branch: '" + gitlabTargetBranchName + "', url: '" + repositoryUrl + "'\n" +
                        "            }\n" +
                        "        }\n" +
                        "\n" +
                        "        stage('Build Backend') {\n" +
                        "            when {\n" +
                        "                changeset pattern: 'backend/.*', comparator: 'REGEXP'\n" +
                        "            }\n" +
                        "            steps {\n" +
                        "                echo '2. Backend 변경 감지, 빌드 및 배포'\n" +
                        "                withCredentials([file(credentialsId: \"backend\", variable: 'BACKEND_ENV')]) {\n" +
                        "                    sh '''\n" +
                        "                        set -e\n" +
                        "                        echo \"  - 복사: $BACKEND_ENV → ${WORKSPACE}/backend/.env\"\n" +
                        "                        cp \"\\$BACKEND_ENV\" \"\\$WORKSPACE/backend/.env\"\n" +
                        "                    '''\n" +
                        "                }\n" +
                        "                dir('backend') {\n" +
                        "                    sh '''\n" +
                        "                        set -e\n" +
                        "                        chmod +x gradlew\n" +
                        "                        ./gradlew clean build -x test\n" +
                        "                        docker build -t spring-app .\n" +
                        "                        docker stop my-spring-app || true\n" +
                        "                        docker rm my-spring-app || true\n" +
                        "                        docker run -d -p 8080:8080 --env-file .env --name my-spring-app spring-app\n" +
                        "                    '''\n" +
                        "                }\n" +
                        "                echo '[INFO] 백엔드 완료'\n" +
                        "            }\n" +
                        "        }\n" +
                        "\n" +
                        "        stage('Build Frontend') {\n" +
                        "            when {\n" +
                        "                changeset pattern: 'frontend/.*', comparator: 'REGEXP'\n" +
                        "            }\n" +
                        "            steps {\n" +
                        "                echo '3. Frontend 변경 감지, 빌드 및 배포'\n" +
                        "                withCredentials([file(credentialsId: \"front\", variable: 'FRONT_ENV')]) {\n" +
                        "                    sh '''\n" +
                        "                        set -e\n" +
                        "                        echo \"  - 복사: $FRONT_ENV → ${WORKSPACE}/frontend/.env\"\n" +
                        "                        cp \"\\$FRONT_ENV\" \"\\$WORKSPACE/frontend/.env\"\n" +
                        "                    '''\n" +
                        "                }\n" +
                        "                dir('frontend') {\n" +
                        "                    sh '''\n" +
                        "                        set -e\n" +
                        "                        docker build -t my-next-app .\n" +
                        "                        docker stop frontend || true\n" +
                        "                        docker rm frontend || true\n" +
                        "                        docker run -d --restart unless-stopped --name frontend -p 3000:3000 my-next-app\n" +
                        "                    '''\n" +
                        "                }\n" +
                        "                echo '[INFO] 프론트엔드 완료'\n" +
                        "            }\n" +
                        "        }\n" +
                        "\n" +
                        "        stage('Build AI') {\n" +
                        "            when {\n" +
                        "                changeset pattern: 'ai/.*', comparator: 'REGEXP'\n" +
                        "            }\n" +
                        "            steps {\n" +
                        "                echo '4. AI 변경 감지, 빌드 시작'\n" +
                        "                withCredentials([file(credentialsId: \"ai\", variable: 'AI_ENV')]) {\n" +
                        "                    sh '''\n" +
                        "                        set -e\n" +
                        "                        echo \"  - 복사: $AI_ENV → ${WORKSPACE}/ai/.env\"\n" +
                        "                        cp \"\\$AI_ENV\" \"\\$WORKSPACE/ai/.env\"\n" +
                        "                    '''\n" +
                        "                }\n" +
                        "                dir('ai') {\n" +
                        "                    sh '''\n" +
                        "                        set -e\n" +
                        "                        docker build -t my-ai-app .\n" +
                        "                        docker stop ai || true\n" +
                        "                        docker rm ai || true\n" +
                        "                        docker run -d --restart unless-stopped --name ai -p 8001:8001 -v $(pwd)/app/uploads:/app/uploads --env-file .env my-ai-app\n" +
                        "                    '''\n" +
                        "                }\n" +
                        "                echo '[INFO] AI 완료'\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n" +
                        "EOF\n";

        return List.of(
                "cd /var/lib/jenkins/jobs/auto-created-deployment-job &&" +  "sudo git clone " + repositoryUrl + "&& cd " + projectName,
                jenkinsfileContent,
                "cd " + projectPath + "&& sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "&& sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "&& sudo git add Jenkinsfile",
                "cd " + projectPath + "&& sudo git commit --allow-empty -m 'add Jenkinsfile for CI/CD with SEED'",
                "cd " + projectPath + "&& sudo git push origin " + gitlabTargetBranchName
        );
    }

    private List<String> makeDockerfileForBackend(String repositoryUrl, String projectPath, String gitlabTargetBranchName) {

        log.info(repositoryUrl);

        String dockerfileContent =
                "cd " + projectPath + "/backend && cat <<EOF | sudo tee Dockerfile > /dev/null\n" +
                        "# 1단계: 빌드 스테이지\n" +
                        "FROM gradle:8.5-jdk17 AS builder\n" +
                        "WORKDIR /app\n" +
                        "COPY . .\n" +
                        "RUN gradle bootJar --no-daemon\n" +
                        "\n" +
                        "# 2단계: 실행 스테이지\n" +
                        "FROM openjdk:17-jdk-slim\n" +
                        "WORKDIR /app\n" +
                        "COPY --from=builder /app/build/libs/*.jar app.jar\n" +
                        "CMD [\"java\", \"-jar\", \"app.jar\"]\n" +
                        "EOF\n";

        return List.of(
                "cd " + projectPath + "/backend",
                dockerfileContent,
                "cd " + projectPath + "/backend && sudo git config user.name \"SeedBot\"",
                "cd " + projectPath + "/backend && sudo git config user.email \"seedbot@auto.io\"",
                "cd " + projectPath + "/backend && sudo git add Dockerfile",
                "cd " + projectPath + "/backend && sudo git commit --allow-empty -m 'add Dockerfile for Backend with SEED'",
                "cd " + projectPath + "/backend && sudo git push origin " + gitlabTargetBranchName
        );
    }

    private List<String> makeGitlabWebhook(String gitlabPersonalAccessToken, Long projectId, String jobName, String serverIp, String gitlabTargetBranchName) {
        String hookUrl = "http://" + serverIp + ":9090/project/" + jobName;

        gitlabService.createPushWebhook(gitlabPersonalAccessToken, projectId, hookUrl, gitlabTargetBranchName);

        //최초 실행 로직 한번 필요 그래야 아래 777의미가 있음
        return List.of();
        //return List.of("sudo chmod -R 777 /var/lib/jenkins/workspace");
    }

    private void issueAndSaveToken(Long projectId, String serverIp) {
        try {
            String jenkinsUrl = "http://" + serverIp + ":9090";
            String jenkinsJobName = "auto-created-deployment-job";
            String jenkinsUsername = "admin";
            String jenkinsToken = generateTokenViaCurl(
                    jenkinsUrl,
                    jenkinsUsername,
                    "pwd123",
                    jenkinsUsername
            );

            JenkinsInfo jenkinsInfo = JenkinsInfo.builder()
                    .projectId(projectId)
                    .baseUrl(jenkinsUrl)
                    .username(jenkinsUsername)
                    .apiToken(jenkinsToken)
                    .jobName(jenkinsJobName)
                    .build();

            jenkinsInfoRepository.save(jenkinsInfo);
            log.info("Jenkins API 토큰을 DB에 저장했습니다.");

        } catch (Exception e) {
            log.error("Jenkins 토큰 발급 또는 저장 실패", e);
            throw new BusinessException(ErrorCode.JENKINS_TOKEN_SAVE_FAILED);
        }
    }

    private String generateTokenViaCurl(String jenkinsUrl, String username, String password, String tokenName) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // 쿠키 저장용 임시 파일 생성
            File cookieFile = File.createTempFile("jenkins_cookie", ".txt");
            String cookiePath = cookieFile.getAbsolutePath().replace("\\", "/");

            // 1. Crumb + 쿠키 요청
            List<String> crumbCommand = Arrays.asList(
                    "curl",
                    "-u", username + ":" + password,
                    "-c", cookiePath,
                    "-s",
                    jenkinsUrl + "/crumbIssuer/api/json"
            );

            Process crumbProcess = new ProcessBuilder(crumbCommand)
                    .redirectErrorStream(true).start();

            String crumbResponse = new BufferedReader(new InputStreamReader(crumbProcess.getInputStream()))
                    .lines().collect(Collectors.joining());

            log.info("Crumb 응답: " + crumbResponse);
            if (!crumbResponse.trim().startsWith("{")) {
                throw new BusinessException(ErrorCode.JENKINS_CRUMB_REQUEST_FAILED);
            }

            JsonNode crumbJson = mapper.readTree(crumbResponse);
            String crumb = crumbJson.get("crumb").asText();
            String crumbField = crumbJson.get("crumbRequestField").asText();

            // 2️. 토큰 요청
            String tokenUrl = jenkinsUrl + "/user/" + username + "/descriptorByName/jenkins.security.ApiTokenProperty/generateNewToken";
            String tokenJsonPayload = "{\"newTokenName\":\"" + tokenName + "\"}";

            List<String> curlCommand = Arrays.asList(
                    "curl",
                    "-u", username + ":" + password,
                    "-b", cookiePath,
                    "-c", cookiePath,
                    "-s",
                    "-X", "POST",
                    tokenUrl,
                    "-H", crumbField + ":" + crumb,
                    "-H", "Content-Type: application/json",
                    "-H", "Referer: " + jenkinsUrl + "/",
                    "-d", tokenJsonPayload
            );


            Process tokenProcess = new ProcessBuilder(curlCommand)
                    .redirectErrorStream(true).start();

            String tokenResponse = new BufferedReader(new InputStreamReader(tokenProcess.getInputStream()))
                    .lines().collect(Collectors.joining());


            if (!tokenResponse.trim().startsWith("{")) {
                throw new BusinessException(ErrorCode.JENKINS_TOKEN_RESPONSE_INVALID);
            }

            JsonNode tokenJson = mapper.readTree(tokenResponse);
            String token = tokenJson.path("data").path("tokenValue").asText();

            if (token == null || token.isBlank()) {
                throw new BusinessException(ErrorCode.JENKINS_TOKEN_PARSE_FAILED);
            }

            return token;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JENKINS_TOKEN_REQUEST_FAILED);
        }
    }




    // 서버 초기화
    private List<String> serverResetCommands() {
        return List.of(
                ""
        );
    }

    @Override
    public void resetServer(InitServerRequest request, MultipartFile pemFile) {
        String host = request.getServerIp();
        Session session = null;

        try {
            // 1) 원격 서버 세션 등록
            log.info("세션 생성 시작");
            session = createSessionWithPem(pemFile, host);
            log.info("세션 생성 성공");

            // 2) 명령어 실행
            log.info("초기화 명령 실행 시작");
            for (String cmd : serverResetCommands()) {
                log.info("명령 수행:\n{}", cmd);
                String output = execCommand(session, cmd);
                log.info("명령 결과:\n{}", output);
            }

            // 3) 성공 로그
            log.info("모든 인프라 설정 세팅을 초기화했습니다.");

        } catch (JSchException e) {
            log.error("SSH 연결 실패 (host={}): {}", host, e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        } catch (IOException e) {
            log.error("PEM 파일 로드 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);

        } finally {
            if (session != null && !session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private Session createSessionWithPem(MultipartFile pemFile, String host) throws JSchException, IOException {
        byte[] keyBytes = pemFile.getBytes();

        JSch jsch = new JSch();
        jsch.addIdentity("ec2-key", keyBytes, null, null);

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

            // 2) 채널 연결 타임아웃 (예: 20초)
            channel.connect(20000);

            // 3) 명령 실행 대기 (예: 1분)
            long start = System.currentTimeMillis();
            long maxWait = 10 * 60_000;
            while (!channel.isClosed()) {
                if (System.currentTimeMillis() - start > maxWait) {
                    channel.disconnect();
                    throw new IOException("명령 실행 타임아웃: " + command);
                }
                Thread.sleep(200);
            }

            // 4) 종료 코드 확인
            int code = channel.getExitStatus();
            if (code != 0) {
                throw new IOException(
                        String.format("명령 실패(exit=%d): %s", code, stderr.toString(StandardCharsets.UTF_8))
                );
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
}
