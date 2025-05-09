package org.example.backend.common.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.entity.ProjectStatus;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.domain.project.repository.ProjectStatusRepository;
import org.example.backend.domain.server.entity.HttpsLog;
import org.example.backend.domain.server.repository.HttpsLogRepository;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.response.ApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConvertHttpsUtil {

    private final SshUtil sshUtil;
    private final HttpsLogRepository httpsLogRepository;
    private final RedisSessionManager redisSessionManager;
    private final UserProjectRepository userProjectRepository;
    private final ProjectStatusRepository projectStatusRepository;

    private static final String NGINX_CONF_PATH = "/etc/nginx/sites-available/app";

    public ApiResponse<String> convertHttpToHttps(MultipartFile pem, String host, String domain, String email, Long projectId, String accessToken) {
        Session session = null;
        try {
            validateUserInProject(projectId, accessToken);
            ProjectStatus status = projectStatusRepository.findByProjectId(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_STATUS_NOT_FOUND));
            if (status.isHttpsEnabled()) {
                throw new BusinessException(ErrorCode.HTTPS_ALREADY_ENABLED);
            }

            session = sshUtil.createSessionWithPem(pem, host);
            saveLog(projectId, "Certbot 설치", installCertbot(session));
            saveLog(projectId, "인증서 발급", issueSslCertificate(session, domain, email));
            saveLog(projectId, "Nginx 설정 덮어쓰기", overwriteNginxConf(session, domain));
            saveLog(projectId, "Nginx reload", reloadNginx(session));
            return ApiResponse.success("HTTPS 변환 완료");
        } catch (Exception e) {
            log.error("❌ HTTPS 변환 중 에러 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (session != null) session.disconnect();
        }
    }

    private String installCertbot(Session session) {
        StringBuilder logs = new StringBuilder();
        logs.append(executeCommand(session, "sudo apt update"));
        logs.append(executeCommand(session, "sudo apt install -y certbot python3-certbot-nginx"));
        return logs.toString();
    }

    private String issueSslCertificate(Session session, String domain, String email) {
        String cmd = String.format("sudo certbot --nginx -d %s --email %s --agree-tos --redirect --non-interactive", domain, email);
        return executeCommand(session, cmd);
    }

    private String overwriteNginxConf(Session session, String domain) {
        String conf = generateNginxConf(domain).replace("'", "'\"'\"'");
        String cmd = String.format("echo '%s' | sudo tee %s > /dev/null", conf, NGINX_CONF_PATH);
        return executeCommand(session, cmd);
    }

    private String reloadNginx(Session session) {
        return executeCommand(session, "sudo systemctl reload nginx");
    }

    private String executeCommand(Session session, String command) {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);
            channel.setInputStream(null);
            channel.connect();

            StringBuilder output = new StringBuilder();
            try (InputStream in = channel.getInputStream()) {
                int read;
                byte[] buffer = new byte[1024];
                while ((read = in.read(buffer)) != -1) {
                    output.append(new String(buffer, 0, read));
                }
                while (!channel.isClosed()) {
                    Thread.sleep(100);
                }
                if (channel.getExitStatus() != 0) {
                    throw new BusinessException(ErrorCode.COMMAND_EXECUTION_FAILED);
                }
            }
            channel.disconnect();
            return output.toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.COMMAND_EXECUTION_FAILED);
        }
    }

    private void saveLog(Long projectId, String stepName, String logContent) {
        httpsLogRepository.save(HttpsLog.builder()
                .projectId(projectId)
                .stepName(stepName)
                .logContent(logContent)
                .createdAt(LocalDateTime.now())
                .build());
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

                root /var/www/html;
                index index.html;

                location / {
                    try_files $uri $uri/ /index.html;
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

    private void validateUserInProject(Long projectId, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        if (!userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }
    }
}
