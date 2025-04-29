package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.server.DeleteServerFolderRequest;
import org.example.backend.controller.request.server.DeploymentRegistrationRequest;
import org.example.backend.controller.request.server.InitServerRequest;
import org.example.backend.controller.request.server.NewServerRequest;
import org.example.backend.domain.server.service.ServerService;
import org.springframework.http.HttpHeaders;
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

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> register(
            @RequestPart("request") NewServerRequest newServerRequest,
            @RequestPart("keyFile") MultipartFile keyFile) throws IOException {

        serverService.registerServer(newServerRequest, keyFile);
        return ResponseEntity.ok("등록 완료 및 폴더 생성 완료");
    }

    @DeleteMapping("/delete-folder")
    public ResponseEntity<String> deleteFolder(
            @RequestBody DeleteServerFolderRequest request) {

        serverService.deleteFolderOnServer(request);
        return ResponseEntity.ok("폴더 삭제 완료");
    }

    @PostMapping("/deployment")
    public ResponseEntity<String> registerDeployment(
            @RequestPart("request") DeploymentRegistrationRequest request,
            @RequestPart("pemFile") MultipartFile pemFile,
            @RequestPart("envFile") MultipartFile envFile,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        serverService.registerDeployment(request, pemFile, envFile, accessToken);

        return ResponseEntity.ok("서버 자동 배포 설정 완료");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetServer(
            @RequestPart("request") InitServerRequest request,
            @RequestPart("pemFile") MultipartFile pemFile) {

        serverService.resetServer(request, pemFile);

        return ResponseEntity.ok("서버 초기화 완료");
    }
}
