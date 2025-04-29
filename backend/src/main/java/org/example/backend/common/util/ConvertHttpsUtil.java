package org.example.backend.common.util;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.response.ApiResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class ConvertHttpsUtil {

    private static final String NGINX_CONF_PATH = "/etc/nginx/sites-available/app";

    public ApiResponse<String> convertHttpToHttps(String domain, String email) {
        try {
            installCertbot();
            issueSslCertificate(domain, email);
            overwriteNginxConf(domain);
            reloadNginx();
            return ApiResponse.success("HTTPS 변환 완료");
        } catch (Exception e) {
            log.error("HTTPS 변환 중 에러 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void installCertbot() {
        log.info("Certbot 설치 시작...");
        executeCommand("sudo apt update");
        executeCommand("sudo apt install -y certbot python3-certbot-nginx");
        executeCommand("certbot --version");
        log.info("Certbot 설치 완료");
    }

    private void issueSslCertificate(String domain, String email) {
        log.info("SSL 인증서 발급 시작...");
        executeCommand(String.format(
                "sudo certbot --nginx -d %s --email %s --agree-tos --redirect --non-interactive",
                domain, email
        ));
        log.info("SSL 인증서 발급 완료");
    }

    private void overwriteNginxConf(String domain) {
        log.info("Nginx conf 파일 덮어쓰기 시작...");
        String confContent = generateNginxConf(domain);
        String escapedContent = confContent.replace("'", "'\"'\"'");
        String command = String.format(
                "echo '%s' | sudo tee %s > /dev/null",
                escapedContent, NGINX_CONF_PATH
        );
        executeCommand(command);
        log.info("Nginx conf 파일 덮어쓰기 완료");
    }

    private void reloadNginx() {
        log.info("Nginx reload 시작...");
        executeCommand("sudo systemctl reload nginx");
        log.info("Nginx reload 완료");
    }

    private void executeCommand(String command) {
        log.info("[실행] 명령어: {}", command);
        try {
            Process process = new ProcessBuilder("/bin/sh", "-c", command)
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BusinessException(ErrorCode.COMMAND_EXECUTION_FAILED);
            }
        } catch (IOException | InterruptedException e) {
            throw new BusinessException(ErrorCode.COMMAND_EXECUTION_FAILED);
        }
    }

    private String generateNginxConf(String domain) {
        return String.format(
                """
                server {
                    listen 80;
                    server_name %s;

                    return 301 https://$host$request_uri;
                }

                server {
                    listen 443 ssl http2;
                    server_name %s;

                    ssl_certificate /etc/letsencrypt/live/%s/fullchain.pem; # managed by Certbot
                    ssl_certificate_key /etc/letsencrypt/live/%s/privkey.pem; # managed by Certbot
                    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
                    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

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
                """,
                domain, domain, domain, domain
        );
    }
}