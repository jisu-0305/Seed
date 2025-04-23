package org.example.backend.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
public class GitlabUriBuilder {
    @Value("${gitlab.api.base-url}")
    private String baseUrl;

    public String projects(int page, int perPage) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/projects")
                .queryParam("membership", true)
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .toUriString();
    }

    public URI projectUri(String namespaceAndName) {

        return URI.create(UriComponentsBuilder.fromUriString(baseUrl)
                .path("/projects/")
                .build(false)
                .toUriString() + namespaceAndName);
    }

    public String repositoryTree(Long projectId,
                                 String path,
                                 boolean recursive,
                                 int page,
                                 int perPage) {

        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/projects/{id}/repository/tree")
                .queryParam("path", path)
                .queryParam("recursive", recursive)
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .build(projectId)
                .toString();
    }

    public URI rawFileUri(Long projectId,
                          String filePath,
                          String ref) {

        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .pathSegment(
                        "projects",
                        projectId.toString(),
                        "repository",
                        "files",
                        filePath,
                        "raw"
                )
                .queryParam("ref", ref)
                .encode()
                .build()
                .toUri();
    }

    public URI compare(Long projectId, String from, String to) {
        return URI.create(String.format(
                "%s/projects/%d/repository/compare?from=%s&to=%s",
                baseUrl, projectId, from, to
        ));
    }

}
