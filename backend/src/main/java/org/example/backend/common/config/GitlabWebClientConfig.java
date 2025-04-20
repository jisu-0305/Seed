package org.example.backend.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class GitlabWebClientConfig {

    @Value("${gitlab.api.base-url}")
    private String gitlabApiBaseUrl;

    @Bean("gitlabWebClient")
    public WebClient gitlabWebClient() {
        return WebClient.builder()
                .baseUrl(gitlabApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
