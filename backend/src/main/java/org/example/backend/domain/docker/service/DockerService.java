package org.example.backend.domain.docker.service;

import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.*;

import java.util.List;

public interface DockerService {
    ImageResponse getDockerImages(String image);
    List<TagResponse> getDockerImageTags(String image);
    ImageDefaultPortResponse getDockerImageDefaultPorts(String imageAndTag);
    List<DemonHealthyCheckResponse> checkHealth(String serverIp);
    List<AppHealthyCheckResponse> getAppStatus(String serverIp, String appName);
    List<DockerContainerLogResponse> getContainerLogs(String serverIp, String appName, DockerContainerLogRequest request);
    void controlContainer(String serverIp, String containerName);
    void controlContainer(String serverIp, String containerName, String action);
}
