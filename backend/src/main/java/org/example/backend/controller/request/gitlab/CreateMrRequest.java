package org.example.backend.controller.request.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CreateMrRequest", description = "Merge Request 생성 요청 DTO")
public record CreateMrRequest(
        @NotBlank
        @Schema(description = "소스 브랜치 이름", example = "feature/new-ui")
        String sourceBranch,

        @Schema(description = "타겟(베이스) 브랜치 이름", example = "master", defaultValue = "master")
        String targetBranch,

        @Schema(description = "MR 제목", example = "feat: add new UI", defaultValue = "MR By SEED")
        String title,

        @Schema(description = "MR 상세 설명", example = "로그인 기능 추가 구현 및 테스트 완료")
        String description
) {
    public CreateMrRequest {
        if (targetBranch == null || targetBranch.isBlank()) {
            targetBranch = "master";
        }

        if (title == null || title.isBlank()) {
            title = "MR By SEED";
        }
    }
}
