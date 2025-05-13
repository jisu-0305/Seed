package org.example.backend.domain.docker.service;

import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.*;

import java.util.List;

public interface DockerService {
    ImageResponse getDockerImages(String image);
    List<TagResponse> getDockerImageTags(String image);
    List<DemonHealthyCheckResponse> checkHealth();List<ImageDefaultPortResponse> getDockerImageDefaultPorts(String imageAndTag);
    List<AppHealthyCheckResponse> getAppStatus(String serverIp, String appName);
    List<DockerContainerLogResponse> getContainerLogs(String serverIp, String appName, DockerContainerLogRequest request);
}
