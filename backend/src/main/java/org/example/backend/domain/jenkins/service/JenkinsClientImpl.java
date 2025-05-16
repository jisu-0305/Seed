package org.example.backend.domain.jenkins.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.JenkinsUriBuilder;
import org.example.backend.domain.jenkins.entity.JenkinsInfo;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JenkinsClientImpl implements JenkinsClient {

    private final WebClient jenkinsWebClient;

    public JenkinsClientImpl(@Qualifier("webClient") WebClient jenkinsWebClient) {
        this.jenkinsWebClient = jenkinsWebClient;
    }

    @Override
    public String fetchBuildInfo(JenkinsInfo info, String path) {
        String url = JenkinsUriBuilder.buildBuildInfoUri(info.getBaseUrl(), info.getJobName(), path);
        return safelyRequest(url, info);
    }

    @Override
    public String fetchBuildLog(JenkinsInfo info, int buildNumber) {
        String url = JenkinsUriBuilder.buildConsoleLogUri(info.getBaseUrl(), info.getJobName(), buildNumber);
        return safelyRequest(url, info);
    }

    @Override
    public void triggerBuildWithoutLogin(JenkinsInfo info, String branchName, String originalBranchName) {
        String baseUrl = info.getBaseUrl();
        String jobUrl = JenkinsUriBuilder.buildTriggerUri(baseUrl, info.getJobName()) + "?BRANCH_NAME=" + branchName + "&ORIGINAL_BRANCH_NAME=" + originalBranchName;
        String username = info.getUsername();
        String apiToken = info.getApiToken();

        log.info("Triggering Jenkins build with URL: {}", jobUrl);

        try {
            ObjectMapper mapper = new ObjectMapper();

            // 쿠키 저장용 임시 파일
            File cookieFile = File.createTempFile("jenkins_cookie", ".txt");
            String cookiePath = cookieFile.getAbsolutePath().replace("\\", "/");

            // Crumb 발급
            List<String> crumbCommand = Arrays.asList(
                    "curl", "-u", username + ":" + apiToken,
                    "-c", cookiePath, "-s", baseUrl + "/crumbIssuer/api/json"
            );

            Process crumbProcess = new ProcessBuilder(crumbCommand)
                    .redirectErrorStream(true).start();

            String crumbResponse = new BufferedReader(new InputStreamReader(crumbProcess.getInputStream()))
                    .lines().collect(Collectors.joining());

            if (!crumbResponse.trim().startsWith("{")) {
                log.error("❌ Crumb 응답 오류: {}", crumbResponse);
                throw new BusinessException(ErrorCode.JENKINS_CRUMB_REQUEST_FAILED);
            }

            JsonNode crumbJson = mapper.readTree(crumbResponse);
            String crumb = crumbJson.get("crumb").asText();
            String crumbField = crumbJson.get("crumbRequestField").asText();

            // Trigger build
            List<String> buildCommand = Arrays.asList(
                    "curl", "-X", "POST", jobUrl,
                    "-u", username + ":" + apiToken,
                    "-b", cookiePath,
                    "-H", crumbField + ":" + crumb
            );

            Process buildProcess = new ProcessBuilder(buildCommand)
                    .redirectErrorStream(true).start();

            String buildResponse = new BufferedReader(new InputStreamReader(buildProcess.getInputStream()))
                    .lines().collect(Collectors.joining());

            if (buildResponse.contains("403")) {
                log.error("❌ Jenkins trigger 실패 응답: {}", buildResponse);
                throw new BusinessException(ErrorCode.JENKINS_REQUEST_FAILED);
            }

            log.info("✅ Jenkins 빌드 트리거 성공: 브랜치={}", branchName);

        } catch (Exception e) {
            log.error("❌ Jenkins trigger exception: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.JENKINS_REQUEST_FAILED);
        }
    }

    @Override
    public void triggerBuild(JenkinsInfo info, String branchName) {
        String baseUrl = info.getBaseUrl();
        String jobUrl = JenkinsUriBuilder.buildTriggerUri(baseUrl, info.getJobName()) + "?BRANCH_NAME=" + branchName;
        String username = info.getUsername();
        String apiToken = info.getApiToken();

        log.info("Triggering Jenkins build with URL: {}", jobUrl);

        try {
            ObjectMapper mapper = new ObjectMapper();

            // 쿠키 저장용 임시 파일
            File cookieFile = File.createTempFile("jenkins_cookie", ".txt");
            String cookiePath = cookieFile.getAbsolutePath().replace("\\", "/");

            // Crumb 발급
            List<String> crumbCommand = Arrays.asList(
                    "curl", "-u", username + ":" + apiToken,
                    "-c", cookiePath, "-s", baseUrl + "/crumbIssuer/api/json"
            );

            Process crumbProcess = new ProcessBuilder(crumbCommand)
                    .redirectErrorStream(true).start();

            String crumbResponse = new BufferedReader(new InputStreamReader(crumbProcess.getInputStream()))
                    .lines().collect(Collectors.joining());

            if (!crumbResponse.trim().startsWith("{")) {
                log.error("❌ Crumb 응답 오류: {}", crumbResponse);
                throw new BusinessException(ErrorCode.JENKINS_CRUMB_REQUEST_FAILED);
            }

            JsonNode crumbJson = mapper.readTree(crumbResponse);
            String crumb = crumbJson.get("crumb").asText();
            String crumbField = crumbJson.get("crumbRequestField").asText();

            // Trigger build
            List<String> buildCommand = Arrays.asList(
                    "curl", "-X", "POST", jobUrl,
                    "-u", username + ":" + apiToken,
                    "-b", cookiePath,
                    "-H", crumbField + ":" + crumb
            );

            Process buildProcess = new ProcessBuilder(buildCommand)
                    .redirectErrorStream(true).start();

            String buildResponse = new BufferedReader(new InputStreamReader(buildProcess.getInputStream()))
                    .lines().collect(Collectors.joining());

            if (buildResponse.contains("403")) {
                log.error("❌ Jenkins trigger 실패 응답: {}", buildResponse);
                throw new BusinessException(ErrorCode.JENKINS_REQUEST_FAILED);
            }

            log.info("✅ Jenkins 빌드 트리거 성공: 브랜치={}", branchName);

        } catch (Exception e) {
            log.error("❌ Jenkins trigger exception: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.JENKINS_REQUEST_FAILED);
        }
    }

    // 내부 공통 요청 처리 메서드
    private String safelyRequest(String url, JenkinsInfo info) {
        try {
            return jenkinsWebClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader(info))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("Jenkins 요청 실패: status={}, url={}", clientResponse.statusCode(), url);
                                return clientResponse.createException();
                            })
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Jenkins 요청 예외 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.JENKINS_REQUEST_FAILED);
        }
    }

    private String basicAuthHeader(JenkinsInfo info) {
        String auth = info.getUsername() + ":" + info.getApiToken();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

}
