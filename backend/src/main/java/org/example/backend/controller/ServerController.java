package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.server.*;
import org.example.backend.controller.response.log.HttpsLogResponse;
import org.example.backend.domain.project.service.ProjectService;
import org.example.backend.domain.server.service.HttpsLogService;
import org.example.backend.domain.server.service.ServerService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/server")
public class ServerController {

    private final ServerService serverService;
    private final ProjectService projectService;
    private final HttpsLogService httpsLogService;

    @PostMapping("/deployment")
    public ResponseEntity<ApiResponse<Void>> registerDeployment(
            @RequestParam Long projectId,
            @RequestPart("pemFile") MultipartFile pemFile,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        serverService.registerDeployment(projectId, pemFile, accessToken);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping(value = "/convert")
    public ResponseEntity<ApiResponse<String>> convertHttps(
            @RequestPart HttpsConvertRequest request,
            @RequestPart("pemFile") MultipartFile pemFile,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        serverService.convertHttpToHttps(request, pemFile, accessToken);

        projectService.markHttpsConverted(request.getProjectId());

        return ResponseEntity.ok(ApiResponse.success("https 설정 완료"));
    }

    @Operation(summary = "HTTPS 로그 조회", description = "프로젝트 ID 기준으로 HTTPS 설정 로그를 조회합니다.")
    @GetMapping("/{projectId}")
    public ApiResponse<List<HttpsLogResponse>> getLogs(
            @PathVariable Long projectId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        List<HttpsLogResponse> logs = httpsLogService.getLogs(projectId, accessToken);

        return ApiResponse.success(logs);
    }
}
