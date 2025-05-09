package org.example.backend.controller.response.docker;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DockerContainerLogResponse", description = "컨테이너 로그 응답 DTO")
public record DockerContainerLogResponse(
        String timestamp, // 타임스탬프 (timestamps=true 일 때) _ example = "2025-05-09T07:12:34.567890123Z"
        String message // 로그 메시지 _ example = "Application started successfully"
) {
    public static DockerContainerLogResponse of(String rawLine, boolean hasTimestamp) {
        if (!hasTimestamp) {
            return new DockerContainerLogResponse(null, rawLine);
        }

        int idx = rawLine.indexOf(' ');
        if (idx > 0) {
            String ts  = rawLine.substring(0, idx);
            String msg = rawLine.substring(idx + 1);
            return new DockerContainerLogResponse(ts, msg);
        } else {
            return new DockerContainerLogResponse(null, rawLine);
        }
    }
}
