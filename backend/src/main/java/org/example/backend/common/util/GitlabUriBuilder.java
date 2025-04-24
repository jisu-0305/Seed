package org.example.backend.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

    public URI rawFileUri(Long projectId, String filePath, String ref) {

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

    public URI createBranch(Long projectId, String branchName, String ref) {

        String b = URLEncoder.encode(branchName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String r = URLEncoder.encode(ref, StandardCharsets.UTF_8)
                .replace("+", "%20");

        String url = String.format(
                "%s/projects/%d/repository/branches?branch=%s&ref=%s",
                baseUrl, projectId, b, r
        );

        return URI.create(url);
    }

    public URI createProjectHook(Long projectId,  String hookUrl, String branchFilter) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "hooks")
                .queryParam("url", hookUrl)
                .queryParam("push_events", true)
                .queryParam("enable_ssl_verification", true);

        Optional.ofNullable(branchFilter)
                .filter(f -> !f.isBlank())
                .ifPresent(f -> b.queryParam("push_events_branch_filter", f));

        return b.build().encode().toUri();

    }

}
