package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.util.ConvertHttpsUtil;
import org.example.backend.controller.request.server.*;
import org.example.backend.controller.response.log.HttpsLogResponse;
import org.example.backend.domain.project.service.ProjectService;
import org.example.backend.domain.server.service.HttpsLogService;
import org.example.backend.domain.server.service.ServerService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/server")
public class ServerController {

    private final ServerService serverService;
    private final ConvertHttpsUtil convertHttpsUtil;
    private final ProjectService projectService;
    private final HttpsLogService httpsLogService;


    @PostMapping("/deployment")
    public ResponseEntity<String> registerDeployment(
            @RequestPart("request") DeploymentRegistrationRequest request,
            @RequestPart("pemFile") MultipartFile pemFile,
            @RequestPart("frontEnvFile") MultipartFile frontEnvFile,
            @RequestPart("backEnvFile") MultipartFile backEnvFile,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        serverService.registerDeployment(request, pemFile, frontEnvFile, backEnvFile, accessToken);

        return ResponseEntity.ok("서버 자동 배포 설정 완료");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetServer(
            @RequestPart("request") InitServerRequest request,
            @RequestPart("pemFile") MultipartFile pemFile) {

        serverService.resetServer(request, pemFile);

        return ResponseEntity.ok("서버 초기화 완료");
    }

    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> convertHttps(
            @RequestPart("pem") MultipartFile pem,
            @RequestPart("host") String host,
            @RequestPart("domain") String domain,
            @RequestPart("email") String email,
            @RequestPart("projectId") String projectId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        ApiResponse<String> result = convertHttpsUtil.convertHttpToHttps(pem, host, domain, email, Long.parseLong(projectId), accessToken);
        projectService.markHttpsConverted(Long.parseLong(projectId));
        return result;
    }

    @Operation(summary = "HTTPS 로그 조회", description = "프로젝트 ID 기준으로 HTTPS 설정 로그를 조회합니다.")
    @GetMapping("/{projectId}")
    public ApiResponse<List<HttpsLogResponse>> getLogs(
            @PathVariable Long projectId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        List<HttpsLogResponse> logs = httpsLogService.getLogs(projectId, accessToken);
        return ApiResponse.success(logs);
    }

}
