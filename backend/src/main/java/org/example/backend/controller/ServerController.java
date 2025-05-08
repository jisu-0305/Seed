package org.example.backend.controller;

import com.jcraft.jsch.Session;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.util.ConvertHttpsUtil;
import org.example.backend.common.util.SshUtil;
import org.example.backend.controller.request.server.*;
import org.example.backend.domain.project.service.ProjectService;
import org.example.backend.domain.server.service.ServerService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/server")
public class ServerController {

    private final ServerService serverService;
    private final ConvertHttpsUtil convertHttpsUtil;
    private final ProjectService projectService;


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
    @Operation(summary = "HTTPS 변환", description = "PEM 파일을 업로드하여 HTTPS 설정")
    public ResponseEntity<ApiResponse<String>> convertHttps(
            @RequestPart("pem") MultipartFile pem,
            @RequestPart("host") String host,
            @RequestPart("domain") String domain,
            @RequestPart("email") String email,
            @RequestPart("projectId") String projectId
    ) {
        ApiResponse<String> result = convertHttpsUtil.convertHttpToHttps(pem, host, domain, email);
        projectService.markHttpsConverted(Long.parseLong(projectId));
        return ResponseEntity.ok(result);
    }
}
