package org.example.backend.domain.server.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import jakarta.websocket.DeploymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.server.DeleteServerFolderRequest;
import org.example.backend.controller.request.server.DeploymentRegistrationRequest;
import org.example.backend.controller.request.server.InitServerRequest;
import org.example.backend.controller.request.server.NewServerRequest;
import org.example.backend.domain.server.entity.ServerInfo;
import org.example.backend.domain.server.repository.ServerInfoRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerServiceImpl implements ServerService {

    private final ServerInfoRepository repository;

    public void registerServer(NewServerRequest newServerRequest, MultipartFile keyFile) throws IOException {
        // 1. key.pem 저장
        String dirPath = System.getProperty("user.dir") + "/keys/";
        new File(dirPath).mkdirs();
        String filePath = dirPath + UUID.randomUUID() + "_" + keyFile.getOriginalFilename();
        File savedFile = new File(filePath);
        keyFile.transferTo(savedFile);

        // 1-1. chmod 400 유사한 파일 권한 제한
        savedFile.setReadable(true, true);      // 소유자만 읽기 가능
        savedFile.setWritable(false, false);    // 쓰기 금지
        savedFile.setExecutable(false, false);  // 실행 금지

        // 2. request + key 경로 → DTO 업데이트
        NewServerRequest completedRequest = newServerRequest.withKeyFilePath(filePath);

        // 3. Entity 생성
        ServerInfo server = ServerInfo.create(completedRequest.ipAddress(), completedRequest.keyFilePath());

        // 4. 저장
        repository.save(server);

        // 5. EC2 SSH 폴더 생성
        createFolderOnEC2(server.getIpAddress(), server.getKeyFilePath(), "/home/ubuntu/test-folder");
    }

    @Override
    public void deleteFolderOnServer(DeleteServerFolderRequest request) {
        String ip = request.ipAddress();

        // 1. DB에서 keyFilePath 조회
        ServerInfo server = repository.findByIpAddress(ip)
                .orElseThrow(() -> new IllegalArgumentException("해당 IP의 서버 정보가 없습니다: " + ip));

        String keyFilePath = server.getKeyFilePath();
        String folderPath = "/home/ubuntu/test-folder"; // 고정 경로 (테스트 전용)

        try {
            JSch jsch = new JSch();
            jsch.addIdentity(keyFilePath);

            Session session = jsch.getSession("ubuntu", ip, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("rm -rf " + folderPath);
            channel.connect();

            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            throw new RuntimeException("EC2 폴더 삭제 실패", e);
        }
    }

    private void createFolderOnEC2(String ip, String pemPath, String folderPath) {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(pemPath);

            Session session = jsch.getSession("ubuntu", ip, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 1. 폴더 만들기 + 2. hello.txt 만들기
            String commands = String.join(" && ", List.of(
                    "mkdir -p " + folderPath,
                    "echo 'Hello World' > " + folderPath + "/hello.txt"
            ));

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(commands);
            channel.connect();

            // 종료
            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            throw new RuntimeException("EC2 폴더 또는 파일 생성 실패", e);
        }
    }


    // 공사중
    @Override
    public void registerDeployment(DeploymentRegistrationRequest request, MultipartFile pemFile, MultipartFile envFile) {
        String host = request.getServerIp();
        Session session = null;

        try {
            // 1) 원격 서버 세션 등록
            log.info("세션 생성 시작");
            session = createSessionWithPem(pemFile, host);
            log.info("세션 생성 성공");

            // 2) 명령어 실행
            log.info("인프라 설정 명령 실행 시작");
            for (String cmd : serverInitializeCommands()) {
                log.info("명령 수행:\n{}", cmd);
                String output = execCommand(session, cmd);
                log.info("명령 결과:\n{}", output);
            }

            // 3) 성공 로그
            log.info("모든 인프라 설정 세팅을 완료했습니다.");

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
            long maxWait = 5 * 60_000;
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

    // 서버 배포 프로세스
    private List<String> serverInitializeCommands() {
        return Stream.of(
                setFirewall(),
                updatePackageManager(),
                setJDK(),
                setNodejs(),
                setDocker(),
                setDockerCompose(),
                setNginx(),
                setJenkins(),
                setJenkinsCoufiguration()
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

    // 2. 패키지 업데이트 (apt, apt-get)
    private List<String> updatePackageManager() {
        return List.of(
                "sudo apt update",
                "sudo apt upgrade -y",
                "sudo apt-get update"
        );
    }

    // 3. JDK 설치
    private List<String> setJDK() {
        return List.of(
                "sudo apt install -y default-jdk",
                "java -version"
        );
    }

    // 4. Node.js, npm 설치
    private List<String> setNodejs() {
        return List.of(
                "curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -",
                "sudo apt-get install -y nodejs",
                "node -v",
                "npm -v"
        );
    }

    // 5. Docker 설치
    private List<String> setDocker() {
        return List.of(
                "sudo apt-get install -y ca-certificates curl gnupg",
                "sudo install -m 0755 -d /etc/apt/keyrings",
                "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --batch --yes --no-tty --dearmor -o /etc/apt/keyrings/docker.gpg",
                "echo \\\n" +
                        "  \"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \\\n" +
                        "  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo \"$VERSION_CODENAME\") stable\" | \\\n" +
                        "  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null",
                "sudo systemctl enable docker",
                "sudo systemctl start docker",
                "docker --version"
        );
    }

    // 6. Docker-Compose 설치
    private List<String> setDockerCompose() {
        return List.of(
                "sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",
                "docker compose version"
        );
    }

    // 7. Nginx 설치
    private List<String> setNginx() {
        return List.of(
                "sudo apt install -y nginx",
                "sudo systemctl enable nginx",
                "sudo systemctl start nginx"
        );
    }

    // 7. Jenkins 설치
    private List<String> setJenkins() {
        return List.of(
                "curl -fsSL https://pkg.jenkins.io/debian/jenkins.io-2023.key | sudo tee \\\n" +
                        "  /usr/share/keyrings/jenkins-keyring.asc > /dev/null",
                "echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \\\n" +
                        "  https://pkg.jenkins.io/debian binary/ | \\\n" +
                        "  sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null",
                "sudo apt install -y jenkins",
                "sudo systemctl enable jenkins",
                "sudo systemctl start jenkins",
                "echo \"jenkins version:\" && jenkins --version"
        );
    }

    // 8. Jenkins 상세 설정
    private List<String> setJenkinsCoufiguration() {
        return List.of(
                // 6-1) Setup Wizard 비활성화
                "sudo sed -i '/^#JAVA_ARGS=/a JAVA_ARGS=\"$JAVA_ARGS -Djenkins.install.runSetupWizard=false\"' /etc/default/jenkins",

                // 6-2) init.groovy.d 디렉터리 생성
                "sudo mkdir -p /var/lib/jenkins/init.groovy.d",
                "sudo chmod 644 /var/lib/jenkins/init.groovy.d/*.groovy",

                // 6-3) Groovy init 스크립트로 관리자 계정 자동 생성
                "sudo tee /var/lib/jenkins/init.groovy.d/basic-security.groovy > /dev/null << 'EOF'\n" +
                        "import jenkins.model.*\n" +
                        "import hudson.security.*\n" +
                        "import jenkins.security.*\n" +
                        "import jenkins.security.apitoken.*\n" +
                        "import hudson.model.User\n" +
                        "\n" +
                        "def instance = Jenkins.getInstance()\n" +
                        "\n" +
                        "// 내장 사용자 DB 사용\n" +
                        "def hudsonRealm = new HudsonPrivateSecurityRealm(false)\n" +
                        "def adminId  = System.getenv('JENKINS_ADMIN_ID') ?: 'admin'\n" +
                        "def adminPwd = System.getenv('JENKINS_ADMIN_PASSWORD') ?: 'admin'\n" +
                        "hudsonRealm.createAccount(adminId, adminPwd)\n" +
                        "instance.setSecurityRealm(hudsonRealm)\n" +
                        "\n" +
                        "// 로그인한 사용자에게 모든 권한 부여\n" +
                        "def strategy = new FullControlOnceLoggedInAuthorizationStrategy()\n" +
                        "strategy.setAllowAnonymousRead(false)\n" +
                        "instance.setAuthorizationStrategy(strategy)\n" +
                        "instance.save()\n" +
                        "\n" +
                        "// ===== API Token 자동 생성 및 파일 저장 =====\n" +
                        "def user = User.get(adminId)\n" +
                        "def tokenProperty = user.getProperty(ApiTokenProperty.class)\n" +
                        "def tokenStore = tokenProperty.tokenStore\n" +
                        "def result = tokenStore.generateNewToken(\"init-generated-token\")\n" +
                        "user.save()\n" +
                        "\n" +
                        "def tokenFile = new File(\"/var/lib/jenkins/init_admin_token.txt\")\n" +
                        "tokenFile.write(\"Admin ID: ${adminId}\\nToken: ${result.plainValue}\\n\")\n" +
                        "EOF",

                // 6-4) plugin 다운로드
                "curl -L https://github.com/jenkinsci/plugin-installation-manager-tool/releases/download/2.12.13/jenkins-plugin-manager-2.12.13.jar -o jenkins-plugin-cli.jar",

                "java -jar jenkins-plugin-cli.jar --war /usr/share/java/jenkins.war \\\n" +
                        "--plugin-download-directory=/var/lib/jenkins/plugins \\\n" +
                        "--plugins gitlab-plugin github git workflow-aggregator docker-workflow credentials-binding blueocean configuration-as-code",

                // 6-5) Port 9090으로 변경
                "sudo sed -i 's/^#*HTTP_PORT=.*/HTTP_PORT=9090/' /etc/default/jenkins",
                "sudo sed -i 's/Environment=\"JENKINS_PORT=[0-9]\\+\"/Environment=\"JENKINS_PORT=9090\"/' /usr/lib/systemd/system/jenkins.service",

                // 6-6) 재시작
                "sudo systemctl restart jenkins"
        );
    }

    // 서버 초기화
    private List<String> serverResetCommands() {
        return List.of(
                "sudo apt purge $(apt-mark showmanual) -y",
                "sudo apt autoremove -y",
                "rm -rf ~/.config ~/.local ~/.cache"
        );
    }
}

