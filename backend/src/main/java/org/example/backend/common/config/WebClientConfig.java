package org.example.backend.common.config;

import io.netty.channel.Channel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.kqueue.KQueueDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
public class WebClientConfig {

    @Value("${gitlab.api.base-url}")
    private String gitlabApiBaseUrl;

    @Value("${docker.hub.api.base-url}")
    private String dockerHubApiBaseUrl;

    @Value("${jenkins.api.base-url}")
    private String jenkinsApiBaseUrl;

    @Value("${docker.registry.api.base-url}")
    private String dockerRegistryApiBaseUrl;

    @Value("${docker.auth.api.base-url}")
    private String dockerAuthApiBaseUrl;

    @Value("${docker.engine.api.base-url}")
    private String dockerEngineApiBaseUrl;

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

    // 윈도우일 떄 -> tcp 엔드포인트로 도커 엔진에 연결(도커 설정에서 tcp 열어놔야함)
    @ConditionalOnProperty(name = "docker.mode", havingValue = "tcp")
    @Bean("dockerWebClient")
    public WebClient tcpDockerWebClient() {
        return WebClient.builder()
                .baseUrl(dockerEngineApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @ConditionalOnProperty(name = "docker.mode", havingValue = "socket")
    @Bean("dockerWebClient")
    public WebClient unixDockerWebClient() {
        Class<? extends Channel> channelType =
                Epoll.isAvailable() ? EpollDomainSocketChannel.class : KQueueDomainSocketChannel.class;

        TcpClient tcpClient = TcpClient.create()
                .bootstrap(b -> b.channel(channelType))
                .remoteAddress(() -> new DomainSocketAddress(dockerEngineSocketUrl));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .defaultHeader(HttpHeaders.HOST, "localhost")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("dockerHubWebClient")
    public WebClient dockerHubWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
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

    @Bean("jenkinsWebClient")
    public WebClient jenkinsWebClient() {
        return WebClient.builder()
                .baseUrl(jenkinsApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
