package org.example.backend.common.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.response.ApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConvertHttpsUtil {

    private final SshUtil sshUtil;

    private static final String NGINX_CONF_PATH = "/etc/nginx/sites-available/app";

    public ApiResponse<String> convertHttpToHttps(MultipartFile pem, String host, String domain, String email) {
        Session session = null;
        try {
            session = sshUtil.createSessionWithPem(pem, host);
            installCertbot(session);
            issueSslCertificate(session, domain, email);
            overwriteNginxConf(session, domain);
            reloadNginx(session);
            return ApiResponse.success("HTTPS 변환 완료");
        } catch (Exception e) {
            log.error("❌ HTTPS 변환 중 에러 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private void installCertbot(Session session) {
        log.info("📦 Certbot 설치 시작...");
        executeCommand(session, "sudo apt update");
        executeCommand(session, "sudo apt install -y certbot python3-certbot-nginx");
        log.info("📦 Certbot 설치 완료");
    }

    private void issueSslCertificate(Session session, String domain, String email) {
        log.info("🔐 SSL 인증서 발급...");
        String command = String.format("sudo certbot --nginx -d %s --email %s --agree-tos --redirect --non-interactive", domain, email);
        executeCommand(session, command);
        log.info("🔐 인증서 발급 완료");
    }

    private void overwriteNginxConf(Session session, String domain) {
        log.info("📝 Nginx 설정 덮어쓰기...");
        String conf = generateNginxConf(domain).replace("'", "'\"'\"'");
        String command = String.format("echo '%s' | sudo tee %s > /dev/null", conf, NGINX_CONF_PATH);
        executeCommand(session, command);
    }

    private void reloadNginx(Session session) {
        log.info("🔁 Nginx reload...");
        executeCommand(session, "sudo systemctl reload nginx");
        log.info("🔁 완료");
    }

    private void executeCommand(Session session, String command) {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);
            channel.setInputStream(null);
            channel.connect();

            try (InputStream in = channel.getInputStream()) {
                while (!channel.isClosed()) {
                    Thread.sleep(100);
                }
                if (channel.getExitStatus() != 0) {
                    throw new BusinessException(ErrorCode.COMMAND_EXECUTION_FAILED);
                }
            }
            channel.disconnect();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.COMMAND_EXECUTION_FAILED);
        }
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
}
