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

    public URI buildPushWebhookUri(Long gitlabProjectId, String hookUrl, String wildcard) {
        return toUri(builder("projects", gitlabProjectId.toString(), "hooks")
                .queryParam("url", hookUrl)
                .queryParam("push_events", true)
                .queryParam("enable_ssl_verification", true)
                .queryParam("push_events_branch_filter", wildcard));
    }

    public URI buildCommitUri(Long gitlabProjectId) {
        return toUri(builder("projects", gitlabProjectId.toString(), "repository", "commits"));
    }

    public URI buildMergeRequestUri(Long gitlabProjectId) {
        return toUri(builder("projects", gitlabProjectId.toString(), "merge_requests"));
    }

    public URI buildCreateBranchUri(Long gitlabProjectId, String branchName, String baseBranchName) {
        return toUri(
                builder("projects", gitlabProjectId.toString(), "repository", "branches")
                        .queryParam("branch", branchName)
                        .queryParam("ref", baseBranchName)
        );
    }

    public URI buildDeleteBranchUri(Long gitlabProjectId, String branchName) {
        return toUri(builder("projects", gitlabProjectId.toString(),"repository", "branches", branchName));
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

    public URI buildListMergedMrsUri(Long gitlabProjectId, int page, int perPage) {
        return toUri(builder("projects", gitlabProjectId.toString(), "merge_requests")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .queryParam("order_by", "created_at")
                .queryParam("sort", "desc")
                .queryParam("state", "merged")
        );
    }

    public URI buildMrDetailUri(Long gitlabProjectId, Long mergeRequestIid) {
        return toUri(builder("projects", gitlabProjectId.toString(), "merge_requests", mergeRequestIid.toString()));
    }

    public URI buildCompareCommitsUri(Long gitlabProjectId, String from, String to) {
        return toUri(
                builder("projects", gitlabProjectId.toString(), "repository", "compare")
                        .queryParam("from", from)
                        .queryParam("to", to)
        );
    }

    public URI buildRepositoryTreeUri(
            Long gitlabProjectId,
            String path,
            boolean recursive,
            int page,
            int perPage,
            String branchName
    ) {
        return toUri(
                builder("projects", gitlabProjectId.toString(), "repository", "tree")
                        .queryParam("path", path)
                        .queryParam("recursive", recursive)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .queryParam("ref", branchName)
        );
    }

    public URI buildRawFileUri(Long gitlabProjectId, String filePath, String refBranch) {
        return toUri(
                builder("projects", gitlabProjectId.toString(), "repository", "files", filePath, "raw")
                        .queryParam("ref", refBranch)
        );
    }

    public URI buildBranchUri(Long gitlabProjectId, String branchName) {
        return toUri(builder("projects", gitlabProjectId.toString(),"repository", "branches", branchName));
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
