package org.example.backend.domain.docker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.response.docker.DemonInfoResponse;
import org.example.backend.controller.response.docker.DemonUnHealthyResponse;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.domain.docker.dto.DockerImage;
import org.example.backend.domain.docker.dto.DockerTag;
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

    @Override
    public ImageResponse getImages(String image) {
        log.debug(">>>>> searchRepositories,{}", image);
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
    public List<TagResponse> getTag(String image) {
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
    public List<DemonUnHealthyResponse> checkHealth() {
        log.debug(">>>>> checkHealth (도커 데몬)");

        DemonInfoResponse info = dockerApiClient.getInfo();
        if (info == null) {
            throw new BusinessException(ErrorCode.DOCKER_HEALTH_FAILED);
        }

        int pausedCount  = info.getContainersPaused();
        int stoppedCount = info.getContainersStopped();

        if (pausedCount + stoppedCount == 0) {
            return List.of();
        }

        try {
            List<String> statuses = Arrays.asList("paused", "exited");
            return dockerApiClient.getContainersByStatus(statuses).stream()
                    .map(c -> new DemonUnHealthyResponse(
                            c.getImage(),
                            c.getImageId()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Docker health check failed", e);
            throw new BusinessException(ErrorCode.DOCKER_HEALTH_FAILED);
        }
    }

}
