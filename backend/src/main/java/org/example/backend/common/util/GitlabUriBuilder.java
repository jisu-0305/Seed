package org.example.backend.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GitlabUriBuilder {
    @Value("${gitlab.api.base-url}")
    private String baseUrl;   // e.g. https://gitlab.com/api/v4

    public String projects(int page, int perPage) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
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

        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/projects/{id}/repository/tree")
                .queryParam("path", path)
                .queryParam("recursive", recursive)
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .build(projectId)
                .toString();
    }

    public String rawFile(Long projectId,
                          String encodedPath,
                          String ref) {

        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/projects/{id}/repository/files/{file_path}/raw")
                .queryParam("ref", ref)
                .build(projectId, encodedPath)
                .toString();
    }
}
