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
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DockerApiClientImpl implements DockerApiClient {

    private final WebClient dockerHubWebClient;
    private final WebClient dockerWebClient;
    private final DockerUriBuilder uriBuilder;

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
        try {
            return dockerWebClient.get()
                    .uri(uriBuilder.containersByStatus(statuses))
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
        try {
            return dockerWebClient.get()
                    .uri(uriBuilder.containersByName(nameFilter))
                    .retrieve()
                    .bodyToFlux(ContainerDto.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DOCKER_HEALTH_API_FAILED);
        }
    }

}
