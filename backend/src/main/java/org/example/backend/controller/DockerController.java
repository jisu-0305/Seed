package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.response.docker.AppHealthyCheckResponse;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.controller.response.docker.DemonHealthyCheckResponse;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docker")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Docker", description = "도커 API")
public class DockerController {

    private final DockerService dockerService;

    @GetMapping("/images/{image}")
    @Operation(summary = "도커 이미지 조회_공식만", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<ImageResponse>> searchDockerImages(
            @Parameter(description = "검색어 (예: ubuntu, nginx)", example = "redis")
            @PathVariable("image") String image) {
        ImageResponse response = dockerService.getImages(image);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/images/{image}/tags")
    @Operation(summary = "도커 이미지 태그 조회 (linux/amd64 필터링)", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<TagResponse>>> searchDockerImageTags(
            @Parameter(description = "검색어 (예: ubuntu, nginx)", example = "redis")
            @PathVariable("image") String image) {
        List<TagResponse> responses = dockerService.getTag(image);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 도커 헬스체크
    // 1. 데몬 상태 조회해서 이상한 상태의 개수로 파악
    @GetMapping("/healthy")
    @Operation(
            summary = "도커 전체 헬스 체크",
            description = "ContainersPaused 또는 ContainersStopped 값이 0보다 크면 해당 컨테이너의 Image, ImageID를 리스트로 반환합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    public ResponseEntity<ApiResponse<List<DemonHealthyCheckResponse>>> checkDockerDemonHealth() {
        log.info(">>>>> info 조회");
        List<DemonHealthyCheckResponse> unhealthy = dockerService.checkHealth();
        return ResponseEntity.ok(ApiResponse.success(unhealthy));
    }
    
    // 2. 특정 어플리케이션의 상태 파악
    @GetMapping("/healthy/{appName}")
    public ResponseEntity<ApiResponse<List<AppHealthyCheckResponse>>> checkDockerHealth(
            @Parameter(description = "애플리케이션 이름 (컨테이너 이름 필터)", example = "nginx")
            @PathVariable("appName") String appName) {
        List<AppHealthyCheckResponse> statuses = dockerService.getAppStatus(appName);
        return ResponseEntity.ok(ApiResponse.success(statuses));
    }
}
