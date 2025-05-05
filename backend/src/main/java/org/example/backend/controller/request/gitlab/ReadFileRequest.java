package org.example.backend.controller.request.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "FileRequest", description = "파일 원본 조회 요청 DTO")
public record ReadFileRequest (
        @NotBlank
        @Schema(description = "가져올 파일 경로 (예: src/main/java/…)", example = "backend/build.gradle")
        @RequestParam("filePath")
        String filePath,

        @NotBlank
        @Schema(description = "가져올 파일의 브랜치 또는 커밋 SHA", example = "dev", defaultValue= "master")
        @RequestParam(defaultValue = "master")
        String branch
){}
