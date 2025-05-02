package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.log.DockerLogRequest;
import org.example.backend.controller.response.log.DockerLogResponse;
import org.example.backend.domain.selfcicd.service.SelfCICDService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/self-cicd")
@RequiredArgsConstructor
@Slf4j
public class SelfCICDController {

    private final SelfCICDService selfCICDService;

    @PostMapping(value = "/docker-log", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DockerLogResponse>> getRecentDockerLogs(
            @RequestPart("request") DockerLogRequest request,
            @RequestPart("keyFile") MultipartFile keyFile
    ) throws IOException {

        // PEM 파일 저장
        String dirPath = System.getProperty("user.dir") + "/keys/";
        new File(dirPath).mkdirs();
        String filePath = dirPath + UUID.randomUUID() + "_" + keyFile.getOriginalFilename();
        File savedFile = new File(filePath);
        keyFile.transferTo(savedFile);

        // 경로 주입해서 요청 완성
        request.setPemPath(filePath);
        DockerLogResponse response = selfCICDService.getRecentDockerLogs(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}