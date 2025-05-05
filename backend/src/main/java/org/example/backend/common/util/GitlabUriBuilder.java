package org.example.backend.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Slf4j
@Component
public class GitlabUriBuilder {

    @Value("${gitlab.api.base-url}")
    private String baseUrl;

    public URI projects(int page, int perPage) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects")
                .queryParam("membership", true)
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .encode()
                .build()
                .toUri();
    }

    public URI projectUri(String namespaceAndName) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", namespaceAndName)
                .encode()
                .build()
                .toUri();
    }

    public URI repositoryTree(Long projectId,
                              String path,
                              boolean recursive,
                              int page,
                              int perPage) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "repository", "tree")
                .queryParam("path", path)
                .queryParam("recursive", recursive)
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .encode()
                .build()
                .toUri();
    }

    public URI rawFileUri(Long projectId, String filePath, String ref) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "repository", "files", filePath, "raw")
                .queryParam("ref", ref)
                .encode()
                .build()
                .toUri();
    }

    public URI compare(Long projectId, String from, String to) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "repository", "compare")
                .queryParam("from", from)
                .queryParam("to", to)
                .encode()
                .build()
                .toUri();
    }

    public URI createBranch(Long projectId, String branchName, String ref) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "repository", "branches")
                .queryParam("branch", branchName)
                .queryParam("ref", ref)
                .encode()
                .build()
                .toUri();
    }

    public URI deleteBranch(Long projectId, String branchName) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "repository", "branches", branchName)
                .encode()
                .build()
                .toUri();
    }

    public URI createMergeRequest(Long projectId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "merge_requests")
                .encode()
                .build()
                .toUri();
    }

    public URI getBranchUri(Long projectId, String branchName) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "repository", "branches", branchName)
                .encode()
                .build()
                .toUri();
    }

    public URI createProjectHook(Long projectId, String hookUrl, String branchFilter) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "hooks")
                .queryParam("url", hookUrl)
                .queryParam("push_events", true)
                .queryParam("enable_ssl_verification", true);

        Optional.ofNullable(branchFilter)
                .filter(f -> !f.isBlank())
                .ifPresent(f -> builder.queryParam("push_events_branch_filter", f));

        return builder.encode().build().toUri();
    }

    public URI listMergeRequests(Long projectId,
                                 int page,
                                 int perPage) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "merge_requests")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .queryParam("order_by", "created_at")
                .queryParam("sort", "desc")
                .queryParam("state", "merged")
                .encode()
                .build()
                .toUri();
    }

    public URI getMergeRequest(Long projectId, Long mergeRequestIid) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "merge_requests", mergeRequestIid.toString())
                .encode()
                .build()
                .toUri();
    }

    public URI createCommit(Long projectId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("projects", projectId.toString(), "repository", "commits")
                .encode()
                .build()
                .toUri();
    }

}
