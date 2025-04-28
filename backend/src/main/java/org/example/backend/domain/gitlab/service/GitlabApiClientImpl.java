package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.GitlabUriBuilder;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.GitlabBranch;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitlabApiClientImpl implements GitlabApiClient {

    private final WebClient gitlabWebClient;
    private final GitlabUriBuilder uriBuilder;

    @Override
    public List<GitlabProject> listProjects(String accessToken) {
        List<GitlabProject> projects;
        String uri =uriBuilder.projects(1, 100);
        try {
            projects = gitlabWebClient.get().uri(uri)
                    .headers(header -> header.setBearerAuth(accessToken))
                    .retrieve().bodyToFlux(GitlabProject.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            if (e instanceof WebClientResponseException &&
                    ((WebClientResponseException) e).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error("listProjects 메서드에서 예외 발생", e);
            throw new BusinessException(ErrorCode.GITLAB_BAD_PROJECTS);
        }
        return projects;
    }

    @Override
    public GitlabProject getProjectInfo(String token, String projectUrlOrPath) {
        String path = toProjectPath(projectUrlOrPath);
        URI uri = uriBuilder.projectUri(path);

        log.debug(">>>>> final URI = {}", uri);

        try {
            return gitlabWebClient.get().uri(uri)
                    .headers(header -> header.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(GitlabProject.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error("getProjectInfo 예외", ex);
            throw new BusinessException(ErrorCode.GITLAB_BAD_PROJECTS);
        }
    }

    @Override
    public List<GitlabTree> listTree(String accessToken, Long projectId, String path, boolean recursive) {
        List<GitlabTree> tree;
        String uri = uriBuilder.repositoryTree(projectId, path, recursive, 1, 100);

        try {
            tree = gitlabWebClient.get().uri(uri)
                    .headers(header -> header.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToFlux(GitlabTree.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            if (e instanceof WebClientResponseException &&
                    ((WebClientResponseException) e).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error("listTree 메서드에서 예외 발생", e);
            throw new BusinessException(ErrorCode.GITLAB_BAD_TREE);
        }
        return tree;
    }

    @Override
    public String getRawFile(String accessToken, Long projectId, String path, String ref) {
        String content;
        URI uri = uriBuilder.rawFileUri(projectId, path, ref);
        log.debug(">>>>>>>>> getRawFile URI = {}", uri);

        try {
            content = gitlabWebClient.get().uri(uri)
                    .headers(header -> header.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            if (e instanceof WebClientResponseException &&
                    ((WebClientResponseException) e).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            log.error("getRawFile 메서드에서 예외 발생", e);
            throw new BusinessException(ErrorCode.GITLAB_BAD_FILE);
        }
        return content;
    }

    @Override
    public GitlabCompareResponse compareCommits(String accessToken, Long projectId, String from, String to) {

        URI uri = uriBuilder.compare(projectId, from, to);

        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(GitlabCompareResponse.class)
                    .block();
        } catch (WebClientResponseException e) {

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_COMPARE);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GITLAB_BAD_COMPARE);
        }
    }

    @Override
    public GitlabBranch createBranch(String accessToken,
                                     Long projectId,
                                     String branch,
                                     String ref) {
        URI uri = uriBuilder.createBranch(projectId, branch, ref);
        log.debug(">>>>>>>> createBranch URI = {}", uri);

        try {
            return gitlabWebClient.post()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(GitlabBranch.class)
                    .block();

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_CREATE_BRANCH);
        }
    }

    @Override
    public void createProjectHook(String privateToken, Long projectId,
                                               String hookUrl, String pushEventsBranchFilter ) {

        URI uri = uriBuilder.createProjectHook(projectId, hookUrl, pushEventsBranchFilter);
        log.debug(">>>> createProjectHook URI = {}", uri);

        try {
            gitlabWebClient.post()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(privateToken))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("GitLab createProjectHook Error: status={}, body={}, uri={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), uri);
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_CREATE_WEBHOOK);
        }
    }

    @Override
    public void deleteBranch(String accessToken,
                             Long projectId,
                             String branch) {
        URI uri = uriBuilder.deleteBranch(projectId, branch);
        log.debug(">>>>>>>> deleteBranch URI = {}", uri);

        try {
            gitlabWebClient.delete()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
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
        log.debug(">>>>>>> createMergeRequest URI = {}", uri);

        BodyInserters.FormInserter<String> form = BodyInserters
                .fromFormData("source_branch", sourceBranch)
                .with("target_branch", targetBranch)
                .with("title", title);

        if (description != null && !description.isBlank()) {
            form = form.with("description", description);
        }

        try {
            return gitlabWebClient.post()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(accessToken))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .bodyToMono(MergeRequestCreateResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new BusinessException(ErrorCode.GITLAB_MERGE_REQUEST_FAILED);
        }
    }

    @Override
    public GitlabBranch getBranch(String accessToken, Long projectId, String branchName) {
        URI uri = uriBuilder.getBranchUri(projectId, branchName);
        try {
            return gitlabWebClient.get()
                    .uri(uri)
                    .headers(header -> header.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(GitlabBranch.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException(ErrorCode.GITLAB_BRANCH_NOT_FOUND);
            }
            throw new BusinessException(ErrorCode.GITLAB_BAD_REQUEST);
        }
    }

    private static String toProjectPath(String raw) {
        String path = raw.startsWith("http") ? URI.create(raw).getPath() : raw;

        if (path.startsWith("/")) path = path.substring(1);

        return URLEncoder.encode(path, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

}
