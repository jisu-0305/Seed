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

    @Value("${docker.auth.api.base-url}")
    private String dockerAuthApiBaseUrl;

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

    public URI buildContainerLogsUri(
            String containerId,
            Boolean includeStdout,
            Boolean includeStderr,
            String tailLines,
            Long sinceSeconds,
            Long untilSeconds,
            Boolean includeTimestamps,
            Boolean includeDetails,
            Boolean followStream
    ) {
        UriComponentsBuilder uri = UriComponentsBuilder
                .fromUriString(dockerEngineApiBaseUrl)
                .path("/containers/{id}/logs")
                .queryParam("stdout", includeStdout)
                .queryParam("stderr", includeStderr)
                .queryParam("tail", tailLines)
                .queryParam("timestamps", includeTimestamps)
                .queryParam("details", includeDetails)
                .queryParam("follow", followStream);

        if (sinceSeconds != null) {
            uri.queryParam("since", sinceSeconds);
        }
        if (untilSeconds != null) {
            uri.queryParam("until", untilSeconds);
        }

        return uri.buildAndExpand(containerId).toUri();
    }

    /**
     * 매니페스트 조회용 URI
     * GET /v2/{namespace}/{imageName}/manifests/{tag}
     */
    public URI buildRegistryManifestUri(String namespace, String imageName, String tag) {
        return UriComponentsBuilder
                .fromUriString(registryApiBaseUrl)
                .pathSegment(namespace, imageName, "manifests", tag)
                .build()
                .encode()
                .toUri();
    }

    /**
     * 블랍(config) 조회용 URI
     * GET /v2/{namespace}/{imageName}/blobs/{blobHashId}
     *
     */
    public URI buildRegistryBlobUri(String namespace, String imageName, String blobHashId) {
        return UriComponentsBuilder
                .fromUriString(registryApiBaseUrl)
                .pathSegment(namespace, imageName, "blobs", blobHashId)
                .build()
                .encode()
                .toUri();
    }

    /**
     * Docker Hub 토큰 발급용 URI 생성
     * GET {docker.auth.api.base-url}/token?service=registry.docker.io&scope=repository:{namespace}/{imageName}:pull
     */
    public URI buildRegistryAuthUri(String namespace, String imageName) {
        return UriComponentsBuilder
                .fromUriString(dockerAuthApiBaseUrl)
                .path("/token")
                .queryParam("service", "registry.docker.io")
                .queryParam("scope", "repository:" + namespace + "/" + imageName + ":pull")
                .build()
                .toUri();
    }

    private URI buildFilteredContainersUri(String key, List<String> values) {
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
