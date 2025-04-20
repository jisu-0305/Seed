package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.GitlabUriBuilder;
import org.example.backend.domain.gitlab.dto.GitlabProjectDto;
import org.example.backend.domain.gitlab.dto.GitlabTreeItemDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitlabApiClientImpl implements GitlabApiClient {

    @Qualifier("gitlabWebClient")
    private final WebClient webClient;

    private final GitlabUriBuilder uriBuilder;

    @Override
    public List<GitlabProjectDto> listProjects(String pat) {
        return webClient.get()
                .uri(uriBuilder.projects(1, 100))
                .header("PRIVATE-TOKEN", pat)
                .retrieve()
                .bodyToFlux(GitlabProjectDto.class)
                .collectList()
                .block();
    }

    @Override
    public List<GitlabTreeItemDto> listTree(String pat,
                                            Long projectId,
                                            String path,
                                            boolean recursive) {
        String uri = uriBuilder.repositoryTree(projectId, path, recursive, 1, 100);
        log.debug("listTree URI = {}", uri);

        return webClient.get()
                .uri(uri)
                .header("PRIVATE-TOKEN", pat)
                .retrieve()
                .bodyToFlux(GitlabTreeItemDto.class)
                .collectList()
                .block();
    }

    @Override
    public String getRawFile(String pat,
                             Long projectId,
                             String path,
                             String ref) {

        URI uri = uriBuilder.rawFileUri(projectId, path, ref);
        log.debug(">>>>>>>>> getRawFile URI = {}", uri);

        return webClient.get()
                .uri(uri)
                .header("PRIVATE-TOKEN", pat)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
