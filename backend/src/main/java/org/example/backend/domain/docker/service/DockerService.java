package org.example.backend.domain.docker.service;

import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.domain.docker.dto.DockerTagByRegistry;

import java.util.List;

public interface DockerService {
    ImageResponse getImages(String query, int page, int pageSize);
    List<TagResponse> getTag(String image);
}
