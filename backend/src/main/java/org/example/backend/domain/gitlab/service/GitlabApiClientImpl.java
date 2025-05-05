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

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitlabApiClientImpl implements GitlabApiClient {

    private final WebClient gitlabWebClient;
    private final GitlabUriBuilder uriBuilder;

    @Override
    public List<GitlabProject> listProjects(String accessToken) {
        URI uri = uriBuilder.projects(1, 100);
        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToFlux(GitlabProject.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error("listProjects error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_PROJECTS);
        }
    }

    @Override
    public GitlabProject getProjectInfo(String token, String projectPath) {
        URI uri = uriBuilder.projectUri(projectPath);
        log.debug("getProjectInfo URI = {}", uri);
        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(GitlabProject.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error("getProjectInfo error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_PROJECTS);
        }
    }

    @Override
    public List<GitlabTree> listTree(String accessToken, Long projectId, String path, boolean recursive) {
        URI uri = uriBuilder.repositoryTree(projectId, path, recursive, 1, 100);
        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToFlux(GitlabTree.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error("listTree error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_TREE);
        }
    }

    @Override
    public String getRawFile(String accessToken, Long projectId, String path, String ref) {
        URI uri = uriBuilder.rawFileUri(projectId, path, ref);
        log.debug("getRawFile URI = {}", uri);
        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error("getRawFile error", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_FILE);
        }
    }

    @Override
    public GitlabCompareResponse compareCommits(String accessToken, Long projectId, String from, String to) {
        URI uri = uriBuilder.compare(projectId, from, to);
        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(GitlabCompareResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_COMPARE);
        }
    }

    @Override
    public GitlabBranch createBranch(String accessToken, Long projectId, String branch, String ref) {
        URI uri = uriBuilder.createBranch(projectId, branch, ref);
        log.debug("createBranch URI = {}", uri);
        try {
            return gitlabWebClient.post()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(GitlabBranch.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_CREATE_BRANCH);
        }
    }

    @Override
    public void createProjectHook(String accessToken, Long projectId, String hookUrl, String branchFilter) {
        URI uri = uriBuilder.createProjectHook(projectId, hookUrl, branchFilter);
        try {
            gitlabWebClient.post()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("createProjectHook error status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_CREATE_WEBHOOK);
        }
    }

    @Override
    public void deleteBranch(String accessToken, Long projectId, String branch) {
        URI uri = uriBuilder.deleteBranch(projectId, branch);
        log.debug("deleteBranch URI = {}", uri);
        try {
            gitlabWebClient.delete()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_DELETE_BRANCH);
        }
    }

    @Override
    public MergeRequestCreateResponse createMergeRequest(
            String accessToken,
            Long projectId,
            String sourceBranch,
            String targetBranch,
            String title,
            String description
    ) {
        URI uri = uriBuilder.createMergeRequest(projectId);
        log.debug("createMergeRequest URI = {}", uri);
        var form = BodyInserters.fromFormData("source_branch", sourceBranch)
                .with("target_branch", targetBranch)
                .with("title", title);
        if (description != null && !description.isBlank()) {
            form = form.with("description", description);
        }
        try {
            return gitlabWebClient.post()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .bodyToMono(MergeRequestCreateResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new BusinessException(ErrorCode.GITLAB_MERGE_REQUEST_FAILED);
        }
    }

    @Override
    public void getBranch(String accessToken, Long projectId, String branchName) {
        URI uri = uriBuilder.getBranchUri(projectId, branchName);
        try {
            gitlabWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(GitlabBranch.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException(ErrorCode.GITLAB_BRANCH_NOT_FOUND);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
        }
    }

    @Override
    public List<GitlabMergeRequest> listMergeRequests(String accessToken, Long projectId, int page, int perPage) {
        URI uri = uriBuilder.listMergeRequests(projectId, page, perPage);
        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToFlux(GitlabMergeRequest.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_MERGE_REQUESTS);
        }
    }

    @Override
    public GitlabMergeRequest getMergeRequest(String accessToken, Long projectId, Long mergeRequestIid) {
        URI uri = uriBuilder.getMergeRequest(projectId, mergeRequestIid);
        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(GitlabMergeRequest.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException(ErrorCode.GITLAB_MR_NOT_FOUND);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
        }
    }

    @Override
    public void createCommit(String accessToken, Long projectId, String branch, String message, List<CommitAction> actions) {
        URI uri = uriBuilder.createCommit(projectId);
        var payload = Map.of(
                "branch", branch,
                "commit_message", message,
                "actions", actions
        );
        try {
            gitlabWebClient.post()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new BusinessException(ErrorCode.GITLAB_BAD_CREATE_COMMIT);
        }
    }

}
