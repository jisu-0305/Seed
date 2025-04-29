package org.example.backend.domain.docker.service;

import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.domain.docker.dto.DockerTag;

public interface DockerApiClient {
    ImageResponse getImages(String query, int page, int pageSize);
    DockerTag getTags(String namespace, String repo, int page, int pageSize);
}
