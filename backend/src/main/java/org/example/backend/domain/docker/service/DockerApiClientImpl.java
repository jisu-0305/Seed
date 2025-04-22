package org.example.backend.domain.docker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.DockerUriBuilder;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class DockerApiClientImpl implements DockerApiClient {

    private final WebClient dockerHubWebClient;
//    private final WebClient dockerAuthWebClient;
//    private final WebClient dockerRegistryWebClient;
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
    public TagResponse getTags(String namespace, String repo, int page, int pageSize) {
        try {
            return dockerHubWebClient.get()
                    .uri(uriBuilder.listTags(namespace, repo, page, pageSize))
                    .retrieve()
                    .bodyToMono(TagResponse.class)
                    .block();
        }  catch (Exception e) {
            throw new BusinessException(ErrorCode.DOCKER_TAGS_API_FAILED);
        }
    }
}
