package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.*;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

    @GetMapping("/servers/{serverIp:.+}/healthy")
    @Operation(
            summary = "도커 불량 컨테이너 조회",
            description = "ContainersPaused 또는 ContainersStopped 값이 0보다 크면 해당 컨테이너 정보 반환함")
    public ResponseEntity<ApiResponse<List<DemonHealthyCheckResponse>>> checkDockerDemonHealth(@PathVariable("serverIp") String serverIp) {
        List<DemonHealthyCheckResponse> unhealthy = dockerService.checkHealth(serverIp);
        return ResponseEntity.ok(ApiResponse.success(unhealthy));
    }

    @GetMapping("/servers/{serverIp:.+}/healthy/{appName}")
    @Operation(summary = "특정 이릅을 가지는 컨테이너들의 헬시 상태 조회",
            description = "(backend 검색시 -> backend-docker, backend-test 등등)")
    public ResponseEntity<ApiResponse<List<AppHealthyCheckResponse>>> checkDockerHealth(
            @PathVariable("serverIp") String serverIp,
            @Parameter(description = "애플리케이션 이름 (컨테이너 이름 필터)", example = "redis")
            @PathVariable("appName") String appName) {
        List<AppHealthyCheckResponse> statuses = dockerService.getAppStatus(serverIp, appName);
        return ResponseEntity.ok(ApiResponse.success(statuses));
    }

    @GetMapping("/servers/{serverIp:.+}/logs/{appName}")
    @Operation(summary = "컨테이너 로그 조회")
    public ResponseEntity<ApiResponse<List<DockerContainerLogResponse>>> getContainerLogs(
            @PathVariable("serverIp") String serverIp,
            @Parameter(description = "컨테이너명", example = "backend") @PathVariable("appName") String appName,
            @Validated @ModelAttribute DockerContainerLogRequest request){
        List<DockerContainerLogResponse> logs = dockerService.getContainerLogs(serverIp, appName, request);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
