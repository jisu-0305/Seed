package org.example.backend.domain.docker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.cache.DockerTokenCacheManager;
import org.example.backend.common.util.DockerUriBuilder;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DockerApiClientImpl implements DockerApiClient {

    private final WebClient dockerHubWebClient;
    private final WebClient dockerRegistryWebClient;
    private final DockerUriBuilder uriBuilder;
    private final DockerTokenCacheManager tokenCacheManager;

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

    @Override
    public List<String> getRegistryTagNames(String namespace,
                                            String repo,
                                            int n,
                                            String last) {
        String token = tokenCacheManager.getAnonymousToken(namespace, repo);
        String uri = uriBuilder.listRegistryTags(namespace, repo, n, last);

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = dockerRegistryWebClient.get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (resp == null || resp.get("tags") == null) {
            throw new BusinessException(ErrorCode.DOCKER_TAGS_API_FAILED);
        }

        return (List<String>) resp.get("tags");
    }

}
