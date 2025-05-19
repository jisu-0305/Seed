package org.example.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

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

    private final ExchangeStrategies largeBufferExchangeStrategies = ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
            .build();

    @Bean("webClient")
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(largeBufferExchangeStrategies)
                .build();
    }

    @Bean("gitlabWebClient")
    public WebClient gitlabWebClient() {
        return WebClient.builder()
                .baseUrl(gitlabApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(largeBufferExchangeStrategies)
                .build();
    }

    @Bean("dockerWebClientBuilder")
    public WebClient.Builder dockerWebClientBuilder() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(largeBufferExchangeStrategies);
    }

    @Bean("dockerHubWebClient")
    public WebClient dockerHubWebClient() {
        return WebClient.builder()
                .baseUrl(dockerHubApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(largeBufferExchangeStrategies)
                .build();
    }

    @Bean("dockerRegistryWebClient")
    public WebClient dockerRegistryWebClient() {
        return WebClient.builder()
                .baseUrl(dockerRegistryApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(largeBufferExchangeStrategies)
                .build();
    }

    @Bean("dockerAuthWebClient")
    public WebClient dockerAuthWebClient() {
        return WebClient.builder()
                .baseUrl(dockerAuthApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(largeBufferExchangeStrategies)
                .build();
    }

}