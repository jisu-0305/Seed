package org.example.backend.domain.docker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.*;
import org.example.backend.domain.docker.dto.ContainerDto;
import org.example.backend.domain.docker.dto.DockerImage;
import org.example.backend.domain.docker.dto.DockerTag;
import org.example.backend.domain.docker.enums.ContainerActionType;
import org.example.backend.domain.project.entity.Application;
import org.example.backend.domain.project.repository.ApplicationEnvVariableListRepository;
import org.example.backend.domain.project.repository.ApplicationRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerServiceImpl implements DockerService {

    private final DockerApiClient dockerApiClient;
    private final ApplicationRepository applicationRepository;
    private final ApplicationEnvVariableListRepository applicationEnvVariableListRepository;

    @Override
    public ImageResponse getDockerImages(String image) {
        int page = 1;
        int pageSize = 100;

        ImageResponse imageResponse = dockerApiClient.getImages(image, page, pageSize);

        if (imageResponse == null) {
            throw new BusinessException(ErrorCode.DOCKER_SEARCH_FAILED);
        }

        try {
            List<DockerImage> officialImage = imageResponse.getImage().stream()
                    .filter(DockerImage::isOfficial)
                    .collect(Collectors.toList());
            imageResponse.setImage(officialImage);
            imageResponse.setCount(officialImage.size());
            return imageResponse;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DOCKER_SEARCH_FAILED);
        }
    }

    @Override
    public List<TagResponse> getDockerImageTags(String image) {
        String namespace = "library";
        int page = 1;
        int pageSize = 100;

        DockerTag dockerTag = dockerApiClient.getTags(namespace, image, page, pageSize);
        if (dockerTag == null) {
            throw new BusinessException(ErrorCode.DOCKER_TAGS_FAILED);
        }

        return dockerTag.getResults().stream()
                .filter(item -> item.getImages().stream().anyMatch(img ->
                        "amd64".equals(img.getArchitecture()) &&
                                "linux".equals(img.getOs())
                ))

                .map(item -> new TagResponse(
                        item.getName(),
                        item.getRepository(),
                        item.isV2(),
                        item.getDigest()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<DemonHealthyCheckResponse> checkHealth(String serverIp) {

        DemonContainerStateCountResponse info = dockerApiClient.getInfo(serverIp);

        if (info == null) {
            throw new BusinessException(ErrorCode.DOCKER_HEALTH_FAILED);
        }

        int pausedCount  = info.getContainersPaused();
        int stoppedCount = info.getContainersStopped();
        if (pausedCount + stoppedCount == 0) {
            return List.of();
        }

        try {
            List<String> statuses = List.of("paused","exited");
            return dockerApiClient.getContainersByStatus(serverIp, statuses).stream()
                    .map(container -> {
                        String rawName = container.getNames().stream().findFirst().orElse("");
                        String name = rawName.startsWith("/") ? rawName.substring(1) : rawName;

                        return new DemonHealthyCheckResponse(
                                name,
                                container.getState(),
                                container.getImage(),
                                container.getImageId()
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("도커 service_checkHealth 실패", e);
            throw new BusinessException(ErrorCode.DOCKER_HEALTH_FAILED);
        }
    }

    @Override
    public List<AppHealthyCheckResponse> getAppStatus(String serverIp, String appName) {

        try {
            var containers = dockerApiClient.getContainersByName(serverIp, appName);

            if (containers.isEmpty()) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format("애플리케이션 이름 '%s' 에 해당하는 컨테이너가 없습니다.", appName));
            }

            return containers.stream()
                    .map(container -> {
                        String rawName = container.getNames().stream().findFirst().orElse("");
                        String name = rawName.startsWith("/") ? rawName.substring(1) : rawName;

                        return new AppHealthyCheckResponse(
                                name,
                                container.getImage(),
                                container.getImageId(),
                                container.getState(),
                                container.getStatus()
                        );
                    })
                    .collect(Collectors.toList());
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("도커 getAppStatus 실패_ appName={}", appName, e);
            throw new BusinessException(ErrorCode.DOCKER_HEALTH_API_FAILED);
        }
    }

    @Override
    public List<DockerContainerLogResponse> getContainerLogs(String serverIp, String appName, DockerContainerLogRequest request) {

        String containerId = resolveContainerId(serverIp, appName);
        List<String> rawLines = dockerApiClient.getContainerLogs(serverIp, containerId, request);

        return rawLines.stream()
                .map(DockerContainerLogResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public void controlContainer(String serverIp, String containerName) {
        controlContainer(serverIp, containerName, null);
    }

    @Override
    public void controlContainer(String serverIp, String containerName, String action) {

        String containerId = resolveContainerId(serverIp, containerName);
        ContainerActionType actionType = ContainerActionType.from(action);

        try {
            switch (actionType) {
                case STOP  -> dockerApiClient.stopContainer(serverIp, containerId);
                case PAUSE -> dockerApiClient.pauseContainer(serverIp, containerId);
                case RUN   -> dockerApiClient.startContainer(serverIp, containerId);
            }
        } catch (Exception e) {
            log.error("controlContainer 실패 action={} name={}", action, containerName, e);
            throw new BusinessException(ErrorCode.DOCKER_CONTROL_FAILED);
        }
    }

    @Override
    public ImageDefaultPortResponse getDockerImageDefaultPorts(String imageAndTag) {
        String namespace = "library";

        String[] parts = imageAndTag.split(":", 2);
        String imageName = parts[0];
        String tag = (parts.length == 2 && !parts[1].isBlank()) ? parts[1] : "latest";
        String fullName   = imageName + ":" + tag;

        List<String> defaultPorts = dockerApiClient.getImageDefaultPorts(namespace, imageName, tag);

        List<Application> apps = applicationRepository.findByImageName(imageName);

        List<String> imageEnvs = List.of();
        if (!apps.isEmpty()) {
            Long appId = apps.get(0).getId();
            List<String> rawLists = applicationEnvVariableListRepository
                    .findEnvVariableListByApplicationId(appId);

            imageEnvs = rawLists.stream()
                    .flatMap(raw -> Arrays.stream(raw.split(",")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        return new ImageDefaultPortResponse(fullName, defaultPorts, imageEnvs);
    }

    /* 공통 로직 */
    private String resolveContainerId(String serverIp, String containerName) {
        List<ContainerDto> containers = dockerApiClient.getContainersByName(serverIp, containerName);
        if (containers.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    String.format("애플리케이션 이름 '%s' 에 해당하는 컨테이너가 없습니다.", containerName)
            );
        }
        return containers.get(0).getId();
    }


}
