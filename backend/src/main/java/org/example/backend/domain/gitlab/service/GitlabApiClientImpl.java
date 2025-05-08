package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.GitlabUriBuilder;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatusCode;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitlabApiClientImpl implements GitlabApiClient {

    private final WebClient gitlabWebClient;
    private final GitlabUriBuilder uriBuilder;

    /* Push _ webhook 생성 */
    @Override
    public void registerPushWebhook(String gitlabPersonalAccessToken, Long projectId, String hookUrl, String branchFilter) {

        URI uri = uriBuilder.buildPushWebhookUri(projectId, hookUrl, branchFilter);

        Map<String, Object> body = Map.of(
                "url", hookUrl,
                "push_events", true,
                "enable_ssl_verification", false,
                "push_events_branch_filter", branchFilter
        );

        try {
            gitlabWebClient.post()
                    .uri(uri)
                    .headers(h -> h.set("Private-Token", gitlabPersonalAccessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error(">>> registerPushWebhook error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_CREATE_WEBHOOK);
        }
    }

    /* Push 트리거 (커밋 날리기) */
    @Override
    public void submitCommit(String gitlabAccessToken, Long projectId, String branch, String message, List<CommitAction> actions) {

        URI uri = uriBuilder.buildCommitUri(projectId);

        var payload = Map.of("branch", branch, "commit_message", message, "actions", actions);

        try {
            gitlabWebClient.post()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(gitlabAccessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException(ErrorCode.GITLAB_BRANCH_NOT_FOUND);
            }
            log.error(">>> submitCommit error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_CREATE_COMMIT);
        }
    }

    /* MR생성 */
    @Override
    public MergeRequestCreateResponse submitMergeRequest(String gitlabAccessToken,
                                                         Long projectId,
                                                         String sourceBranch,
                                                         String targetBranch,
                                                         String title,
                                                         String description
    ) {

        URI uri = uriBuilder.buildMergeRequestUri(projectId);

        var form = BodyInserters.fromFormData("source_branch", sourceBranch)
                .with("target_branch", targetBranch)
                .with("title", title);

        if (description != null && !description.isBlank()) {
            form = form.with("description", description);
        }

        try {
            return gitlabWebClient.post()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(gitlabAccessToken))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .bodyToMono(MergeRequestCreateResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error(">>> submitMergeRequest error", ex);
            throw new BusinessException(ErrorCode.GITLAB_MERGE_REQUEST_FAILED);
        }
    }

    /* 브랜치 생성 */
    @Override
    public GitlabBranch submitBranchCreation(String gitlabAccessToken, Long projectId, String branch, String refBranch) {

        URI uri = uriBuilder.buildCreateBranchUri(projectId, branch, refBranch);

        try {
            return gitlabWebClient.post()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(gitlabAccessToken))
                    .retrieve()
                    .bodyToMono(GitlabBranch.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error(">>> submitBranchCreation error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_CREATE_BRANCH);
        }
    }

    /*브랜치 삭제*/
    @Override
    public void submitBranchDeletion(String gitlabAccessToken, Long projectId, String branch) {

        URI uri = uriBuilder.buildDeleteBranchUri(projectId, branch);

        try {
            gitlabWebClient.delete()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(gitlabAccessToken))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error(">>> submitBranchDeletion error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_DELETE_BRANCH);
        }
    }

    /* 레포지토리 목록 조회 */
    @Override
    public List<GitlabProject> requestProjectList(String gitlabAccessToken, int page, int perPage) {

        URI uri = uriBuilder.buildProjectsUri(page, perPage);

        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(gitlabAccessToken))
                    .retrieve()
                    .bodyToFlux(GitlabProject.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error(">>> listProjects error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_PROJECTS);
        }
    }

    /* 레포지토리 단건 조회 (URL) */
    @Override
    public GitlabProject requestProjectInfo(String gitlabPersonalAccessToken, String projectPath) {

        URI uri = uriBuilder.buildProjectUri(projectPath);

        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(h -> h.set("Private-Token", gitlabPersonalAccessToken))
                    .retrieve()
                    .bodyToMono(GitlabProject.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error(">>> getProjectInfo error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_PROJECTS);
        }
    }

    /* Diff 1 ) 최신 MR 기준 diff 조회 */
    @Override
    public Mono<List<GitlabMergeRequest>> requestMergedMrs(String gitlabAccessToken, Long projectId, int page, int perPage) {

        URI uri = uriBuilder.buildListMergedMrsUri(projectId, page, perPage);

        return gitlabWebClient.get()
                .uri(uri)
                .headers(header -> header.setBearerAuth(gitlabAccessToken))
                .retrieve()
                .bodyToFlux(GitlabMergeRequest.class)
                .collectList()
                .onErrorResume(WebClientResponseException.Unauthorized.class, ex ->
                        Mono.error(new BusinessException(ErrorCode.GITLAB_BAD_REQUEST)))
                .onErrorResume(ex -> Mono.error(new BusinessException(ErrorCode.GITLAB_BAD_MERGE_REQUESTS)));
    }

    @Override
    public Mono<GitlabMergeRequest> requestMrDetail(String gitlabAccessToken, Long projectId, Long mergeRequestIid) {

        URI uri = uriBuilder.buildMrDetailUri(projectId, mergeRequestIid);

        return gitlabWebClient.get()
                .uri(uri)
                .headers(header -> header.setBearerAuth(gitlabAccessToken))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BusinessException(ErrorCode.GITLAB_BAD_REQUEST)))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new BusinessException(ErrorCode.GITLAB_BAD_MERGE_REQUESTS)))
                .bodyToMono(GitlabMergeRequest.class)
                .onErrorResume(Throwable.class,
                        ex -> Mono.error(new BusinessException(ErrorCode.GITLAB_BAD_REQUEST)));
    }

    /* Diff 1, 2 ) 커밋 간 변경사항 조회 */
    @Override
    public Mono<GitlabCompareResponse> requestCommitComparison(String gitlabAccessToken, Long projectId, String from, String to) {

        URI uri = uriBuilder.buildCompareCommitsUri(projectId, from, to);

        return gitlabWebClient.get()
                .uri(uri)
                .headers(header -> header.setBearerAuth(gitlabAccessToken))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BusinessException(ErrorCode.GITLAB_BAD_REQUEST)))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new BusinessException(ErrorCode.GITLAB_BAD_COMPARE)))
                .bodyToMono(GitlabCompareResponse.class);
    }



    /* 레포지토리 tree 구조 조회  */
    @Override
    public List<GitlabTree> requestRepositoryTree(String gitlabAccessToken, Long projectId, String path, boolean recursive, int page, int perPage ) {

        URI uri = uriBuilder.buildRepositoryTreeUri(projectId, path, recursive, page, perPage);

        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(gitlabAccessToken))
                    .retrieve()
                    .bodyToFlux(GitlabTree.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error(">>> listTree error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_TREE);
        }
    }

    /* 파일 원본 조회  */
    @Override
    public String requestRawFileContent(String gitlabAccessToken, Long projectId, String path, String refBranch) {

        URI uri = uriBuilder.buildRawFileUri(projectId, path, refBranch);

        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(gitlabAccessToken))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error(">>> getRawFile error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_FILE);
        }
    }

    /* 브랜치 조회 */
    @Override
    public void validateBranchExists(String gitlabAccessToken, Long projectId, String branchName) {

        URI uri = uriBuilder.buildBranchUri(projectId, branchName);

        try {
            gitlabWebClient.get()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(gitlabAccessToken))
                    .retrieve()
                    .bodyToMono(GitlabBranch.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException(ErrorCode.GITLAB_BRANCH_NOT_FOUND);
            }
            log.error(">>> getBranch error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
        }
    }

}
