package org.example.backend.common.config;

import io.netty.channel.unix.DomainSocketAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
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

//    @Value("${docker.engine.api.base-url}")
//    private String dockerEngineApiBaseUrl;

    @Value("${docker.engine.socket-url}")
    private String dockerEngineSocketUrl;


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

    @Bean("dockerWebClient")
    public WebClient dockerWebClient() {
        HttpClient httpClient = HttpClient.create()
                .remoteAddress(() -> new DomainSocketAddress(dockerEngineSocketUrl));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("dockerHubWebClient")
    public WebClient dockerHubWebClient() {

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)
                )
                .build();

        return WebClient.builder()
                .baseUrl(dockerHubApiBaseUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean("dockerRegistryWebClient")
    public WebClient dockerRegistryWebClient() {
        return WebClient.builder()
                .baseUrl(dockerRegistryApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("dockerAuthWebClient")
    public WebClient dockerAuthWebClient() {
        return WebClient.builder()
                .baseUrl(dockerAuthApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
