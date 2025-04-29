package org.example.backend.domain.jenkins.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.JenkinsUriBuilder;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class JenkinsClientImpl implements JenkinsClient {

    private final WebClient jenkinsWebClient;

    @Value("${jenkins.username}")
    private String username;

    @Value("${jenkins.api-token}")
    private String apiToken;

    @Override
    public String fetchBuildInfo(String jobName, String path) {
        String url = JenkinsUriBuilder.buildBuildInfoUri(jobName, path);
        return safelyRequest(url);
    }

    @Override
    public String fetchBuildLog(String jobName, int buildNumber) {
        String url = JenkinsUriBuilder.buildConsoleLogUri(jobName, buildNumber);
        return safelyRequest(url);
    }

    @Override
    public void triggerBuild(String jobName) {
        String url = JenkinsUriBuilder.buildTriggerUri(jobName);
        try {
            jenkinsWebClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader(username, apiToken))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("Jenkins trigger 요청 실패: status={}, url={}", clientResponse.statusCode(), url);
                                return clientResponse.createException();
                            })
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JENKINS_REQUEST_FAILED);
        }
    }

    private String safelyRequest(String url) {
        try {
            return jenkinsWebClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader(username, apiToken))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("Jenkins 요청 실패: status={}, url={}", clientResponse.statusCode(), url);
                                return clientResponse.createException();
                            })
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JENKINS_REQUEST_FAILED);
        }
    }

    private String basicAuthHeader(String username, String token) {
        String auth = username + ":" + token;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes());
    }
}
