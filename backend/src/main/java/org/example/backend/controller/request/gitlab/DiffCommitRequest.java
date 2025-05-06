package org.example.backend.controller.request.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DiffCommitRequest (
        @NotBlank
        @Schema(description = "비교 시작 커밋 SHA", example = "24a758dd5b7d75488b08b0b16fd024a964916426")
        String from,

        @NotBlank
        @Schema(description = "비교 종료 커밋 SHA", example = "8ad79145fc20c97fbe8e16b225cb90bdbbe1298")
        String to
) {}
