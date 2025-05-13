package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "도커 이미지 조회_공식만")
    public ResponseEntity<ApiResponse<ImageResponse>> searchDockerImages(
            @Parameter(description = "검색어 (예: ubuntu, nginx)", example = "redis")
            @PathVariable("image") String image) {
        ImageResponse response = dockerService.getDockerImages(image);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/images/{image}/tags")
    @Operation(summary = "도커 이미지 태그 조회 (linux/amd64 필터링)")
    public ResponseEntity<ApiResponse<List<TagResponse>>> searchDockerImageTags(
            @Parameter(description = "검색어 (예: ubuntu, nginx)", example = "redis")
            @PathVariable("image") String image) {
        List<TagResponse> responses = dockerService.getDockerImageTags(image);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/images/{imageAndTag}/default-ports")
    @Operation(summary = "도커 이미지 기본 포트 조회", description = "이미지의 태그에 해당하는 기본 포트를 반환함.(태그 생략하면 latest로 처리됨)")
    public ResponseEntity<ApiResponse<List<ImageDefaultPortResponse>>> searchDockerImageDefaultPorts(
            @Parameter(description = "[이미지 이름] 또는 [이름:태그] (태그 생략 시 latest로 처리, 예: nginx, nginx:1.21, nginx:latest)", example = "nginx")
            @PathVariable("imageAndTag") String imageAndTag
    ) {
        List<ImageDefaultPortResponse> responses = dockerService.getDockerImageDefaultPorts(imageAndTag);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/healthy")
    @Operation(
            summary = "도커 전체 헬스 체크",
            description = "ContainersPaused 또는 ContainersStopped 값이 0보다 크면 해당 컨테이너 정보 반환함"
    )
    public ResponseEntity<ApiResponse<List<DemonHealthyCheckResponse>>> checkDockerDemonHealth() {
        List<DemonHealthyCheckResponse> unhealthy = dockerService.checkHealth();
        return ResponseEntity.ok(ApiResponse.success(unhealthy));
    }

    @GetMapping("/servers/{serverIp}/healthy/{appName}")
    public ResponseEntity<ApiResponse<List<AppHealthyCheckResponse>>> checkDockerHealth(
            @PathVariable("serverIp") String serverIp,
            @Parameter(description = "애플리케이션 이름 (컨테이너 이름 필터)", example = "redis")
            @PathVariable("appName") String appName) {
        List<AppHealthyCheckResponse> statuses = dockerService.getAppStatus(serverIp, appName);
        return ResponseEntity.ok(ApiResponse.success(statuses));
    }

    @GetMapping("/servers/{serverIp}/logs/{appName}")
    public ResponseEntity<ApiResponse<List<DockerContainerLogResponse>>> getContainerLogs(
            @PathVariable("serverIp") String serverIp,
            @PathVariable("appName") String appName,
            @Validated @ModelAttribute DockerContainerLogRequest request){
        List<DockerContainerLogResponse> logs = dockerService.getContainerLogs(serverIp, appName, request);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
