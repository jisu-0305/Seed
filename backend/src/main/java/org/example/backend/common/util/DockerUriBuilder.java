package org.example.backend.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class DockerUriBuilder {
    @Value("${docker.hub.api.base-url}")
    private String baseUrl;

    @Value("${docker.registry.api.base-url}")
    private String registryBaseUrl;

    @Value("${docker.auth.api.base-url}")
    private String authBaseUrl;

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

    public String tokenUri(String namespace, String repo) {
        return UriComponentsBuilder.fromUriString(authBaseUrl)
                .path("/token")
                .queryParam("service", "registry.docker.io")
                .queryParam("scope", "repository:" + namespace + "/" + repo + ":pull")
                .toUriString();
    }

    public String manifestUri(String namespace, String repo, String reference) {
        return UriComponentsBuilder.fromUriString(registryBaseUrl)
                .pathSegment(namespace, repo, "manifests", reference)
                .toUriString();
    }

    public String manifestByDigestUri(String namespace, String repo, String digest) {
        // digest(sha256:...)로 manifest를 요청할 때
        return UriComponentsBuilder.fromUriString(registryBaseUrl)
                .pathSegment(namespace, repo, "manifests", digest)
                .toUriString();
    }

    public String blobUri(String namespace, String repo, String digest) {
        return UriComponentsBuilder.fromUriString(registryBaseUrl)
                .pathSegment(namespace, repo, "blobs", digest)
                .toUriString();
    }
}

