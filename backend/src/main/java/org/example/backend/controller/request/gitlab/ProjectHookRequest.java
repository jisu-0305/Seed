package org.example.backend.controller.request.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ProjectHookRequest", description = "깃랩 Push 웹훅 생성 요청 DTO")
public record ProjectHookRequest(
        @NotBlank
        @Schema(description = "푸시 이벤트를 수신할 콜백 URL", example = "https://example.com/webhook")
        String url,

        @Schema(description = "브랜치 필터 와일드카드 패턴 (없으면 전체)", example = "feature/*", defaultValue = "*")
        String wildcard
) {
    public ProjectHookRequest {
        if (wildcard == null || wildcard.isBlank()) {
            wildcard = "*";
        }
    }
}
