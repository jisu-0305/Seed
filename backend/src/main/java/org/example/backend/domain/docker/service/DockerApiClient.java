package org.example.backend.domain.docker.service;

import org.example.backend.controller.response.docker.SearchResponse;
import org.example.backend.controller.response.docker.TagResponse;

import java.util.List;

public interface DockerApiClient {
    SearchResponse search(String query, int page, int pageSize);
    TagResponse listTags(String namespace, String repo, int page, int pageSize);
    List<String> getExposedPorts(String namespace, String repo, String tag, String os, String arch);
}
