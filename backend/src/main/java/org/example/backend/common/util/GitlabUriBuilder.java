package org.example.backend.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

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

}
