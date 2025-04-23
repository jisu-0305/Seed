package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.GitlabUriBuilder;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
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
                    .headers(h -> h.setBearerAuth(accessToken))
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
    public List<GitlabTree> listTree(String accessToken, Long projectId, String path, boolean recursive) {
        List<GitlabTree> tree;
        String uri = uriBuilder.repositoryTree(projectId, path, recursive, 1, 100);
        log.debug(">>>>>> listTree URI = {}", uri);

        try {
            tree = gitlabWebClient.get().uri(uri)
                    .headers(h -> h.setBearerAuth(accessToken))
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
                    .headers(h -> h.setBearerAuth(accessToken))
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
                    .headers(h -> h.setBearerAuth(accessToken))
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

}
