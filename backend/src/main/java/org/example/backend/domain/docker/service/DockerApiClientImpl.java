package org.example.backend.domain.docker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.DockerUriBuilder;
import org.example.backend.controller.response.docker.DemonContainerStateCountResponse;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.domain.docker.dto.ContainerDto;
import org.example.backend.domain.docker.dto.DockerTag;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
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
        URI uri = uriBuilder.buildSearchRepositoriesUri(query, page, pageSize);
        log.debug(">> Docker Hub 이미지 검색 URI: {}", uri);

        return fetchMono(dockerHubWebClient, uri, ImageResponse.class, ErrorCode.DOCKER_SEARCH_API_FAILED);
    }

    @Override
    public DockerTag getTags(String namespace, String repo, int page, int pageSize) {
        URI uri = uriBuilder.buildHubTagsUri(namespace, repo, page, pageSize);
        log.debug(">> Docker Hub 태그 조회 URI: {}", uri);

        return fetchMono(dockerHubWebClient, uri, DockerTag.class, ErrorCode.DOCKER_TAGS_API_FAILED);
    }

    @Override
    public DemonContainerStateCountResponse getInfo() {
        URI uri = uriBuilder.buildInfoUri();
        log.debug(">> Docker 데몬 정보 조회 URI: {}", uri);

        return fetchMono(dockerWebClient, uri, DemonContainerStateCountResponse.class, ErrorCode.DOCKER_HEALTH_API_FAILED);
    }

    @Override
    public List<ContainerDto> getContainersByStatus(List<String> statuses) {
        URI uri = uriBuilder.buildContainersByStatusUri(statuses);
        log.debug(">> 상태별 컨테이너 조회 URI: {}", uri);

        return fetchFlux(dockerWebClient, uri, ContainerDto.class, ErrorCode.DOCKER_HEALTH_API_FAILED);
    }

    @Override
    public List<ContainerDto> getContainersByName(String nameFilter) {
        URI uri = uriBuilder.buildContainersByNameUri(nameFilter);
        log.debug(">> 이름 기반 컨테이너 조회 URI: {}", uri);

        return fetchFlux(dockerWebClient, uri, ContainerDto.class, ErrorCode.DOCKER_HEALTH_API_FAILED);
    }

    /* 공통 로짘 */
    private <T> T fetchMono(WebClient client, URI uri, Class<T> clazz, ErrorCode errorCode) {
        try {
            return client.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(clazz)
                    .block();
        } catch (Exception e) {
            log.error(">> fetchMono 실패 - URI: {}, Error: {}", uri, e.getMessage(), e);
            throw new BusinessException(errorCode);
        }
    }

    private <T> List<T> fetchFlux(WebClient client, URI uri, Class<T> clazz, ErrorCode errorCode) {
        try {
            return client.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(clazz)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error(">> fetchFlux 실패 - URI: {}, Error: {}", uri, e.getMessage(), e);
            throw new BusinessException(errorCode);
        }
    }
}
