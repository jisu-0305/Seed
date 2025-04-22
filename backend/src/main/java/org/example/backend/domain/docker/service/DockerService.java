package org.example.backend.domain.docker.service;

import org.example.backend.controller.response.docker.SearchResponse;
import org.example.backend.controller.response.docker.TagResponse;

import java.util.List;

public interface DockerService {
    SearchResponse searchRepositories(String query, int page, int pageSize);
    TagResponse getTags(String namespace, String repository, int page, int pageSize);
//    List<String> getDefaultPorts(String namespace, String repo, String tag);
    List<String> getDefaultPorts(String namespace, String repo, String tag, String os, String arch);
}

