package org.example.backend.domain.docker.service;

import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.DemonContainerStateCountResponse;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.domain.docker.dto.ContainerDto;
import org.example.backend.domain.docker.dto.DockerTag;

import java.util.List;

public interface DockerApiClient {
    ImageResponse getImages(String query, int page, int pageSize);
    DockerTag getTags(String namespace, String repo, int page, int pageSize);
    DemonContainerStateCountResponse getInfo(String serverIp);
    List<ContainerDto> getContainersByStatus(String serverIp, List<String> statuses);
    List<ContainerDto> getContainersByName(String serverIp, String nameFilter);
    List<String> getContainerLogs(String serverIp, String containerId, DockerContainerLogRequest filter);
    List<String> getImageDefaultPorts(String namespace, String imageName, String tag);
    void startContainer(String serverIp, String containerId);
    void pauseContainer(String serverIp, String containerId);
    void stopContainer(String serverIp, String containerId);
}
