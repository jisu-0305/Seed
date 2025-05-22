package org.example.backend.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ServerAutoDeploymentFileManagerUtil {

    /**
     * NGINX 서버 블록 설정 문자열을 생성합니다.
     *
     * @param serverIp 도메인 또는 서버 IP (server_name)
     * @return 전체 nginx.conf 설정
     */
    public String createHttpNginxConf(String serverIp) {
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
            """,
                serverIp
        );
    }

    /**
     * 도메인(default server_name) 기반 NGINX 설정 문자열을 생성합니다.
     *
     * @param domain 서버 이름(DOMAIN 또는 IP)
     * @return NGINX 서버 블록 설정 문자열
     */
    public String createHttpNginxConfWithDomain(String domain) {
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

    /**
     * 기본 HTTP -> HTTPS 리다이렉트 및 SSL 설정이 포함된 NGINX 서버 블록을 생성합니다.
     *
     * @param domain 서버 도메인 또는 IP
     * @return NGINX 설정 문자열
     */
    public String createHttpsNginxConfWithDomain(String domain) {
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

    /**
     * 프론트엔드 프레임워크에 따른 Docker 스크립트를 생성합니다.
     *
     * @param frontendFramework 프론트엔드 프레임워크 (Vue.js, React, Next.js)
     * @return Docker 빌드 및 배포 스크립트 문자열
     */
    public String createFrontendDockerScript(String frontendFramework) {
        return switch (frontendFramework) {
            case "Vue.js" -> """
                set -e
                docker build -f Dockerfile -t vue .
                docker stop vue || true
                docker rm vue || true
                docker run -d --network mynet --env-file .env --restart unless-stopped --name vue -p 3000:3000 vue
                """;

            case "React" -> """
                set -e
                docker build -f Dockerfile -t react .
                docker stop react || true
                docker rm react || true
                docker run -d --network mynet --env-file .env --restart unless-stopped --name react -p 3000:3000 react
                """;

            default -> """
                set -e
                docker build -f Dockerfile -t next .
                docker stop next || true
                docker rm next || true
                docker run -d --network mynet --env-file .env --restart unless-stopped --name next -p 3000:3000 next
                """;
        };
    }

    /**
     * Jenkins 파이프라인 Job 설정 XML 스크립트를 생성합니다.
     *
     * @param gitRepoUrl             Git 리포지토리 URL
     * @param credentialsId          Jenkins 자격증명 ID
     * @param gitlabTargetBranchName 브랜치 이름
     * @return 실행 가능한 쉘 스크립트 (tee job-config.xml)
     */
    public String createJenkinsPipelineConfigXml(String gitRepoUrl, String credentialsId, String gitlabTargetBranchName) {
        return """
        sudo tee job-config.xml > /dev/null <<'EOF'
        <?xml version='1.1' encoding='UTF-8'?>
        <flow-definition plugin="workflow-job">
          <description>GitLab 연동 자동 배포</description>
          <keepDependencies>false</keepDependencies>
          <properties>
            <hudson.model.ParametersDefinitionProperty>
              <parameterDefinitions>
                <hudson.model.StringParameterDefinition>
                  <name>BRANCH_NAME</name>
                  <defaultValue>%3$s</defaultValue>
                  <description>Git 브랜치 이름</description>
                </hudson.model.StringParameterDefinition>
              </parameterDefinitions>
            </hudson.model.ParametersDefinitionProperty>
          </properties>
          <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
            <scm class="hudson.plugins.git.GitSCM" plugin="git">
              <configVersion>2</configVersion>
              <userRemoteConfigs>
                <hudson.plugins.git.UserRemoteConfig>
                  <url>%1$s</url>
                  <credentialsId>%2$s</credentialsId>
                </hudson.plugins.git.UserRemoteConfig>
              </userRemoteConfigs>
              <branches>
                <hudson.plugins.git.BranchSpec>
                  <name>%3$s</name>
                </hudson.plugins.git.BranchSpec>
              </branches>
            </scm>
            <scriptPath>Jenkinsfile</scriptPath>
            <lightweight>true</lightweight>
          </definition>
          <triggers>
            <com.dabsquared.gitlabjenkins.GitLabPushTrigger plugin="gitlab-plugin">
              <spec/>
              <triggerOnPush>true</triggerOnPush>
              <triggerOnMergeRequest>false</triggerOnMergeRequest>
              <triggerOnNoteRequest>false</triggerOnNoteRequest>
              <triggerOnPipelineEvent>false</triggerOnPipelineEvent>
              <triggerOnAcceptedMergeRequest>false</triggerOnAcceptedMergeRequest>
              <triggerOnClosedMergeRequest>false</triggerOnClosedMergeRequest>
              <triggerOnApprovedMergeRequest>false</triggerOnApprovedMergeRequest>
              <triggerOpenMergeRequestOnPush>never</triggerOpenMergeRequestOnPush>
              <ciSkip>false</ciSkip>
              <setBuildDescription>true</setBuildDescription>
              <addNoteOnMergeRequest>false</addNoteOnMergeRequest>
              <addVoteOnMergeRequest>false</addVoteOnMergeRequest>
              <useCiFeatures>false</useCiFeatures>
              <addCiMessage>false</addCiMessage>
              <branchFilterType>All</branchFilterType>
            </com.dabsquared.gitlabjenkins.GitLabPushTrigger>
          </triggers>
        </flow-definition>
        EOF
        """.formatted(
                gitRepoUrl,
                credentialsId,
                gitlabTargetBranchName
        );
    }


    /**
     * 프론트엔드 프레임워크별로 Dockerfile 생성 스크립트를 반환합니다.
     *
     * @param framework      "Vue.js", "React", "Next.js" 등 프레임워크 이름
     * @param projectPath    서버상의 프로젝트 경로 (예: /var/lib/jenkins/jobs/... )
     * @param directoryName  프론트엔드 디렉토리 이름 (예: "frontend")
     * @return Dockerfile 을 tee 로 생성하는 쉘 스크립트
     */
    public String createFrontendDockerfileContent(String framework, String projectPath, String directoryName) {
        return switch (framework) {
            case "Vue.js" -> String.format("""
            cd %1$s/%2$s && sudo tee Dockerfile > /dev/null <<'EOF'
            FROM node:22-alpine
            WORKDIR /app
            COPY . .
            RUN npm install && npm run build && npm install -g serve
            EXPOSE 3000
            CMD ["serve", "-s", "dist"]
            EOF
            """, projectPath, directoryName);

            case "React" -> String.format("""
            cd %1$s/%2$s && sudo tee Dockerfile > /dev/null <<'EOF'
            FROM node:22-alpine
            WORKDIR /app
            COPY . .
            RUN npm install && npm run build && npm install -g serve
            EXPOSE 3000
            CMD ["serve", "-s", "build"]
            EOF
            """, projectPath, directoryName);

            default -> String.format("""
            cd %1$s/%2$s && sudo tee Dockerfile > /dev/null <<'EOF'
            FROM node:22-alpine AS builder
            WORKDIR /app
            COPY . .
            RUN npm install
            RUN npm run build

            FROM node:22-alpine
            WORKDIR /app
            COPY --from=builder /app ./
            EXPOSE 3000
            CMD ["npm", "run", "start"]
            EOF
            """, projectPath, directoryName);
        };
    }

    /**
     * 백엔드 빌드 도구(Gradle/Maven)에 맞춰 Dockerfile 생성 스크립트를 반환합니다.
     *
     * @param buildTool      "Gradle" 또는 "Maven"
     * @param projectPath    서버상의 프로젝트 경로 (예: /var/lib/jenkins/jobs/...)
     * @param directoryName  백엔드 디렉토리 이름 (예: "backend")
     * @param jdkVersion     사용할 JDK 버전 (예: "17")
     * @return Dockerfile 을 tee 로 생성하는 쉘 스크립트
     */
    public String createBackendDockerfileContent(String buildTool, String projectPath, String directoryName, String jdkVersion) {
        return switch (buildTool) {
            case "Gradle" -> String.format("""
            cd %1$s/%2$s && sudo tee Dockerfile > /dev/null <<'EOF'
            # 1단계: 빌드 스테이지
            FROM gradle:8.5-jdk%3$s AS builder
            WORKDIR /app
            COPY . .
            RUN gradle bootJar --no-daemon

            # 2단계: 실행 스테이지
            FROM openjdk:%3$s-jdk
            WORKDIR /app
            COPY --from=builder /app/build/libs/*.jar app.jar
            CMD ["java", "-jar", "app.jar"]
            EOF
            """,
                    projectPath,     // %1$s
                    directoryName,   // %2$s
                    jdkVersion       // %3$s
            );

            default -> String.format("""
            cd %1$s/%2$s && sudo tee Dockerfile > /dev/null <<'EOF'
            # 1단계: 빌드 스테이지
            FROM maven:3.9.6-eclipse-temurin-%3$s AS builder
            WORKDIR /app
            COPY . .
            RUN mvn clean package -B -q -DskipTests

            # 2단계: 실행 스테이지
            FROM openjdk:%3$s-jdk
            WORKDIR /app
            COPY --from=builder /app/target/*.jar app.jar
            CMD ["java", "-jar", "app.jar"]
            EOF
            """,
                    projectPath,     // %1$s
                    directoryName,   // %2$s
                    jdkVersion       // %3$s
            );
        };
    }

}
