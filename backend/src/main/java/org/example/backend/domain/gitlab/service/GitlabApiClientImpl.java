package org.example.backend.domain.gitlab.service;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.GitlabUriBuilder;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;

@Service
@Slf4j
public class GitlabApiClientImpl implements GitlabApiClient {

    private final WebClient webClient;
    private final GitlabUriBuilder uriBuilder;

    public GitlabApiClientImpl(
            @Qualifier("gitlabWebClient") WebClient webClient,
            GitlabUriBuilder uriBuilder
    ) {
        this.webClient = webClient;
        this.uriBuilder = uriBuilder;
    }

    @Override
    public List<GitlabProject> listProjects(String accessToken) {
        return webClient.get()
                .uri(uriBuilder.projects(1, 100))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToFlux(GitlabProject.class)
                .collectList()
                .block();
    }

    @Override
    public List<GitlabTree> listTree(String accessToken,
                                     Long projectId,
                                     String path,
                                     boolean recursive) {
        String uri = uriBuilder.repositoryTree(projectId, path, recursive, 1, 100);
        log.debug(">>>>>> listTree URI = {}", uri);

        return webClient.get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToFlux(GitlabTree.class)
                .collectList()
                .block();
    }

    @Override
    public String getRawFile(String accessToken,
                             Long projectId,
                             String path,
                             String ref) {

        URI uri = uriBuilder.rawFileUri(projectId, path, ref);
        log.debug(">>>>>>>>> getRawFile URI = {}", uri);

        return webClient.get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
