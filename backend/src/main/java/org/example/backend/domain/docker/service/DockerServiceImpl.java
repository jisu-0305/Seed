package org.example.backend.domain.docker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.domain.docker.dto.DockerImage;
import org.example.backend.domain.docker.dto.DockerTagByRegistry;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerServiceImpl implements DockerService {

    private final DockerApiClient dockerApiClient;

    @Override
    public ImageResponse getImages(String query, int page, int pageSize) {
        log.debug(">>>>> searchRepositories,{},{},{}", query, page, pageSize);

        ImageResponse imageResponse = dockerApiClient.getImages(query, page, pageSize);

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
    public TagResponse getTags(String namespace, String image, int page, int pageSize) {
        log.debug(">>>>> getTags,{},{},{},{}", namespace, image, page, pageSize);

        TagResponse tagResponse = dockerApiClient.getTags(namespace, image, page, pageSize);
        if (tagResponse == null) {
            throw new BusinessException(ErrorCode.DOCKER_TAGS_FAILED);
        }

        return tagResponse;
    }

    @Override
    public DockerTagByRegistry getRegistryTagNames(String namespace, String repo, int page, int pageSize) {
        log.debug(">>>>> getRegistryTagNames namespace={} repo={} page={} pageSize={}",
                namespace, repo, page, pageSize);

        List<String> tags = dockerApiClient.getRegistryTagNames( namespace, repo, pageSize, null);

        return new DockerTagByRegistry(tags);
    }

}
