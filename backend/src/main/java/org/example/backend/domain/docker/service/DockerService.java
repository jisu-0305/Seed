package org.example.backend.domain.docker.service;

import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.domain.docker.dto.DockerTagByRegistry;

public interface DockerService {
    ImageResponse getImages(String query, int page, int pageSize);
    TagResponse getTags(String namespace, String image, int page, int pageSize);
    DockerTagByRegistry getRegistryTagNames(String namespace, String image, int page, int pageSize);
//    List<String> getDefaultPorts(String namespace, String repo, String tag, String os, String arch);
}

