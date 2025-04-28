package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.domain.docker.dto.DockerTagByRegistry;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docker")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Docker", description = "도커 API")
public class DockerController {

    private final DockerService dockerService;

    @GetMapping("/images")
    @Operation(summary = "도커 이미지 조회_공식만", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<ImageResponse>> searchDockerImages(
            @Parameter(description = "검색어 (예: ubuntu, nginx)", example = "redis") @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        ImageResponse response = dockerService.getImages(query, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/images/{namespace}/{image}/tags")
    @Operation(summary = "도커 이미지의 태그 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<TagResponse>> searchDockerImageTags(
            @Parameter(example = "library") @PathVariable String namespace,
            @Parameter(description = "이미지 이름 (예: redis)", example = "redis") @PathVariable String image,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        TagResponse response = dockerService.getTags(namespace, image, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/registry/images/{namespace}/{image}/tags")
    @Operation(
            summary = "Registry API로 조회한 도커 이미지 태그 목록",
            description = "Registry HTTP API(v2)를 통해 해당 이미지의 태그 이름들을 가져옵니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    public ResponseEntity<ApiResponse<DockerTagByRegistry>> listRegistryTagNames(
            @PathVariable String namespace,
            @PathVariable("image") String repo,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        DockerTagByRegistry dto = dockerService.getRegistryTagNames(namespace, repo, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

}
