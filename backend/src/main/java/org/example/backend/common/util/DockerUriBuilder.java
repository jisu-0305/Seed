package org.example.backend.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DockerUriBuilder {

    @Value("${docker.hub.api.base-url}")
    private String dockerHubBaseUrl;

    @Value("${docker.engine.api.base-url}")
    private String dockerEngineApiBaseUrl;

    @Value("${docker.registry.api.base-url}")
    private String registryApiBaseUrl;

    public URI buildSearchRepositoriesUri(String query, int page, int pageSize) {
        return UriComponentsBuilder.fromUriString(dockerHubBaseUrl)
                .path("/search/repositories")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .build()
                .toUri();
    }

    public URI buildHubTagsUri(String namespace, String repo, int page, int pageSize) {
        return UriComponentsBuilder.fromUriString(dockerHubBaseUrl)
                .pathSegment("repositories", namespace, repo, "tags")
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .build()
                .toUri();
    }

    public URI buildRegistryTagsUri(String namespace, String repo) {
        return UriComponentsBuilder.fromUriString(registryApiBaseUrl)
                .pathSegment("v2", namespace, repo, "tags", "list")
                .build()
                .toUri();
    }

    public URI buildInfoUri() {
        return UriComponentsBuilder.fromUriString(dockerEngineApiBaseUrl)
                .path("/info")
                .build()
                .toUri();
    }

    public URI buildContainersByStatusUri(List<String> statuses) {
        return buildFilteredContainersUri("status", statuses);
    }

    public URI buildContainersByNameUri(String name) {
        return buildFilteredContainersUri("name", List.of(name));
    }

    public URI buildFilteredContainersUri(String key, List<String> values) {
        String json = String.format(
                "{\"%s\":[%s]}",
                key,
                values.stream().map(v -> "\"" + v + "\"").collect(Collectors.joining(","))
        );
        String encoded = URLEncoder.encode(json, StandardCharsets.UTF_8);

        String uriString = dockerEngineApiBaseUrl + "/containers/json?all=true&filters=" + encoded;
        return URI.create(uriString);
    }

}
