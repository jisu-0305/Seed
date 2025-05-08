package org.example.backend.controller.request.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TreeRequest", description = "레포지토리 트리 조회 요청 DTO")
public record TreeRequest(
        @Schema(description = "조회할 경로 (빈 문자열이면 루트)")
        String path,

        @Schema(description = "하위 디렉토리까지 재귀적으로 조회할지 여부", example = "true")
        Boolean recursive,

        @Schema(description = "조회할 브랜치 이름", example = "dev")
        String branchName
) {
    public TreeRequest {
        if (path == null) path = "";
        if (recursive == null) recursive = true;
        if (branchName == null) branchName = "master";
    }
}
