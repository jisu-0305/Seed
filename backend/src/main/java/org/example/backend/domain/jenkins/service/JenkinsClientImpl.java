package org.example.backend.domain.jenkins.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.JenkinsUriBuilder;
import org.example.backend.domain.jenkins.entity.JenkinsInfo;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
    public void triggerBuild(JenkinsInfo info) {
        String url = JenkinsUriBuilder.buildTriggerUri(info.getBaseUrl(), info.getJobName());
        try {
            jenkinsWebClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader(info))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("Jenkins trigger 요청 실패: status={}, url={}", clientResponse.statusCode(), url);
                                return clientResponse.createException();
                            })
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Jenkins trigger exception: {}", e.getMessage());
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
        return "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes());
    }
}
