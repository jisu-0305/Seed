package org.example.backend.domain.docker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.DockerUriBuilder;
import org.example.backend.controller.response.docker.DemonInfoResponse;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.domain.docker.dto.ContainerDto;
import org.example.backend.domain.docker.dto.DockerTag;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DockerApiClientImpl implements DockerApiClient {

    private final WebClient dockerHubWebClient;
    private final WebClient dockerWebClient;
    private final DockerUriBuilder uriBuilder;

    @Value("${docker.engine.api.base-url}")
    private String dockerEngineApiBaseUrl;

    @Override
    public ImageResponse getImages(String query, int page, int pageSize) {
        try {
            return dockerHubWebClient.get()
                    .uri(uriBuilder.searchRepositories(query, page, pageSize))
                    .retrieve()
                    .bodyToMono(ImageResponse.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DOCKER_SEARCH_API_FAILED);
        }
    }

    @Override
    public DockerTag getTags(String namespace, String repo, int page, int pageSize) {
        try {
            return dockerHubWebClient.get()
                    .uri(uriBuilder.listTags(namespace, repo, page, pageSize))
                    .retrieve()
                    .bodyToMono(DockerTag.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DOCKER_TAGS_API_FAILED);
        }
    }

    @Override
    public DemonInfoResponse getInfo() {
        try {
            return dockerWebClient.get()
                    .uri(uriBuilder.info())
                    .retrieve()
                    .bodyToMono(DemonInfoResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Docker /info API 실패", e);
            throw new BusinessException(ErrorCode.DOCKER_HEALTH_API_FAILED);
        }
    }

    @Override
    public List<ContainerDto> getContainersByStatus(List<String> statuses) {
        URI uri = uriBuilder.containersByStatus(statuses);
        log.debug(">>>>>> 도커 uri(전체 상태) -> {}", uri);

        try {
            return dockerWebClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(ContainerDto.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Docker /containers/json?filters API 실패함", e);
            throw new BusinessException(ErrorCode.DOCKER_HEALTH_API_FAILED);
        }
    }



    @Override
    public List<ContainerDto> getContainersByName(String nameFilter) {
        URI uri = uriBuilder.containersByName(nameFilter);
        log.debug(">>>>>> 도커 uri(이름으로 검색) -> {}", uri);

        try {
            return dockerWebClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(ContainerDto.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Docker /containers/json?filters API 실패함 (by name)", e);
            throw new BusinessException(ErrorCode.DOCKER_HEALTH_API_FAILED);
        }
    }

}
