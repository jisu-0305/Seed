package org.example.backend.domain.docker.service;

import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;

public interface DockerApiClient {
    ImageResponse getImages(String query, int page, int pageSize);
    TagResponse getTags(String namespace, String repo, int page, int pageSize);
    // List<String> getExposedPorts(String namespace, String repo, String tag, String os, String arch);
}
