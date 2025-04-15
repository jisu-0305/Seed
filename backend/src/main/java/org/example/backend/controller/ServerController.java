package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.server.NewServerRequest;
import org.example.backend.domain.server.service.ServerService;
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
}
