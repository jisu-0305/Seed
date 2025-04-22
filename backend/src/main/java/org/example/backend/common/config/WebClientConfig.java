package org.example.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Value("${gitlab.api.base-url}")
    private String gitlabApiBaseUrl;

    @Value("${docker.hub.api.base-url}")
    private String dockerHubApiBaseUrl;

    @Value("${docker.registry.api.base-url}")
    private String dockerRegistryApiBaseUrl;

    @Value("${docker.auth.api.base-url}")
    private String dockerAuthApiBaseUrl;

    @Bean("webClient")
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean("gitlabWebClient")
    public WebClient gitlabWebClient() {
        return WebClient.builder()
                .baseUrl(gitlabApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // 1) Docker Hub Web API (검색·태그 조회 등)-> 이미지 검색, 태그 목록 조회
    @Bean("dockerHubWebClient")
    public WebClient dockerHubWebClient() {
        return WebClient.builder()
                .baseUrl(dockerHubApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // 2) Registry HTTP API v2 (매니페스트·블롭 조회) -> 매니페스트 조회, CONFIG/LAYER BLOB조회
    @Bean("dockerRegistryWebClient")
    public WebClient dockerRegistryWebClient() {
        HttpClient httpClient = HttpClient.create()
                .followRedirect(true)   // ← 리다이렉트 자동 추적
                .compress(true);        // ← GZIP 자동 해제

        return WebClient.builder()
                .baseUrl(dockerRegistryApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT,
                        "application/vnd.docker.distribution.manifest.v2+json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // 3) Auth API (토큰 발급) -> 레지스트리 PULL 권한용 JWT 토큰 발급
    @Bean("dockerAuthWebClient")
    public WebClient dockerAuthWebClient() {
        return WebClient.builder()
                .baseUrl(dockerAuthApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
