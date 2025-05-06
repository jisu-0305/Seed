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

    public URI buildPushWebhookUri(Long projectId, String hookUrl, String wildcard) {
        return toUri(builder("projects", projectId.toString(), "hooks")
                .queryParam("url", hookUrl)
                .queryParam("push_events", true)
                .queryParam("enable_ssl_verification", true)
                .queryParam("push_events_branch_filter", wildcard));
    }

    public URI buildCommitUri(Long projectId) {
        return toUri(builder("projects", projectId.toString(), "repository", "commits"));
    }

    public URI buildMergeRequestUri(Long projectId) {
        return toUri(builder("projects", projectId.toString(), "merge_requests"));
    }

    public URI buildCreateBranchUri(Long projectId, String branchName, String baseBranchName) {
        return toUri(
                builder("projects", projectId.toString(), "repository", "branches")
                        .queryParam("branch", branchName)
                        .queryParam("ref", baseBranchName)
        );
    }

    public URI buildDeleteBranchUri(Long projectId, String branchName) {
        return toUri(builder("projects", projectId.toString(),"repository", "branches", branchName));
    }

    public URI buildProjectsUri(int page, int perPage) {
        return toUri(
                builder("projects")
                        .queryParam("membership", true)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
        );
    }

    public URI buildProjectUri(String namespaceAndName) {
        return toUri(builder("projects", namespaceAndName));
    }

    public URI buildListMergedMrsUri(Long projectId, int page, int perPage) {
        return toUri(builder("projects", projectId.toString(), "merge_requests")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .queryParam("order_by", "created_at")
                .queryParam("sort", "desc")
                .queryParam("state", "merged")
        );
    }

    public URI buildMrDetailUri(Long projectId, Long mergeRequestIid) {
        return toUri(builder("projects", projectId.toString(), "merge_requests", mergeRequestIid.toString()));
    }

    public URI buildCompareCommitsUri(Long projectId, String from, String to) {
        return toUri(
                builder("projects", projectId.toString(), "repository", "compare")
                        .queryParam("from", from)
                        .queryParam("to", to)
        );
    }

    public URI buildRepositoryTreeUri(Long projectId, String path, boolean recursive, int page, int perPage) {
        return toUri(
                builder("projects", projectId.toString(), "repository", "tree")
                        .queryParam("path", path)
                        .queryParam("recursive", recursive)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
        );
    }

    public URI buildRawFileUri(Long projectId, String filePath, String refBranch) {
        return toUri(
                builder("projects", projectId.toString(), "repository", "files", filePath, "raw")
                        .queryParam("ref", refBranch)
        );
    }

    public URI buildBranchUri(Long projectId, String branchName) {
        return toUri(builder("projects", projectId.toString(),"repository", "branches", branchName));
    }

    /* 공통 로직 */
    private UriComponentsBuilder builder(String... segments) {
        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .pathSegment(segments);
    }

    private URI toUri(UriComponentsBuilder b) {
        return b.encode().build().toUri();
    }

}
