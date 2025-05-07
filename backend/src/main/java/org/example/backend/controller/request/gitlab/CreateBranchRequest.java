package org.example.backend.controller.request.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CreateBranchRequest", description = "새 브랜치 생성 요청 DTO")
public record CreateBranchRequest(
        @NotBlank
        @Schema(description = "새로 만들 브랜치 이름", example = "feature/my-new-branch")
        String branch,

        @NotBlank
        @Schema(description = "생성 기준이 될 브랜치 이름", example = "master")
        String baseBranch
) {}
