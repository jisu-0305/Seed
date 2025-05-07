package org.example.backend.controller.request.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.example.backend.domain.gitlab.dto.PatchedFile;

import java.util.List;

public record CommitPatchedFilesRequest(
        @NotBlank
        @Schema(description = "커밋을 남길 브랜치", example = "pushstest")
        String branch,

        @NotBlank
        @Schema(description = "커밋 메시지", example = "fix: AI auto fix by SEED")
        String commitMessage,

        @NotBlank
        @Schema(description = "AI가 생성한 patched 파일 목록")
        List<PatchedFile> patchedFiles
) {}
