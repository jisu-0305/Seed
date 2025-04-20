package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.GitlabUriBuilder;
import org.example.backend.domain.gitlab.dto.GitlabProjectDto;
import org.example.backend.domain.gitlab.dto.GitlabTreeItemDto;
import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitlabApiClientImpl implements GitlabApiClient {

    /** application.yml 또는 @Configuration 에 정의된, GitLab API 전용 WebClient */
    @Qualifier("gitlabWebClient")
    private final WebClient webClient;

    private final GitlabUriBuilder uriBuilder;


    //private String stripPrefix(String token) {
    //    return token.replaceFirst("(?i)^glpat-", "").trim();
    //}

    @Override
    public List<GitlabProjectDto> listProjects(String pat) {
//        String clean = stripPrefix(pat);
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
//                .headers(h -> h.setBearerAuth(pat))
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
        // GitLab API 요구사항에 따라 파일 경로를 URL-encode
        String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
        String uri = uriBuilder.rawFile(projectId, encodedPath, ref);
        log.debug("getRawFile URI = {}", uri);

        return webClient.get()
                .uri(uri)
//                .headers(h -> h.setBearerAuth(pat))
                .header("PRIVATE-TOKEN", pat)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
