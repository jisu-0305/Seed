package org.example.backend.domain.docker.service;

import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.*;

import java.util.List;

public interface DockerService {
    ImageResponse getImages(String image);
    List<TagResponse> getTag(String image);
    List<DemonHealthyCheckResponse> checkHealth();
    List<AppHealthyCheckResponse> getAppStatus(String appName);
    List<DockerContainerLogResponse> getContainerLogs(String appName, DockerContainerLogRequest request);
}
