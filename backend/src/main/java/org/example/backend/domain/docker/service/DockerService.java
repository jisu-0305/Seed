package org.example.backend.domain.docker.service;

import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;

import java.util.List;

public interface DockerService {
    ImageResponse getImages(String image);
    List<TagResponse> getTag(String image);
}
