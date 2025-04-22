package org.example.backend.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class DockerUriBuilder {

    @Value("${docker.hub.api.base-url}")
    private String baseUrl;

    public String searchRepositories(String query, int page, int pageSize) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/search/repositories")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .toUriString();
    }

    public String listTags(String namespace, String repo, int page, int pageSize) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/repositories")
                .pathSegment(namespace, repo, "tags")
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .toUriString();
    }

}

