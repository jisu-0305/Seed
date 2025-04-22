package org.example.backend.domain.docker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.response.docker.SearchResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerServiceImpl implements DockerService {

    private final DockerApiClient dockerApiClient;

    @Override
    public SearchResponse searchRepositories(String query, int page, int pageSize) {
        // 이따가 예외처리하자
        log.info(">>>>> searchRepositories,{},{},{}", query, page, pageSize);
        return dockerApiClient.search(query, page, pageSize);
    }

    @Override
    public TagResponse getTags(String namespace, String repository, int page, int pageSize) {
        log.info(">>>>> getTags,{},{},{},{}", namespace, repository, page, pageSize);
        return dockerApiClient.listTags(namespace, repository, page, pageSize);
    }

    @Override
    public List<String> getDefaultPorts(String namespace, String repo, String tag, String os, String arch) {
        return dockerApiClient.getExposedPorts(namespace, repo, tag, os, arch);
    }
}
