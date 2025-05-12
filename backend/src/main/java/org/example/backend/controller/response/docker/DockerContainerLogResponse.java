package org.example.backend.controller.response.docker;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DockerContainerLogResponse", description = "컨테이너 로그 응답 DTO")
public record DockerContainerLogResponse(
        String timestamp,
        String message
) {
    public static DockerContainerLogResponse of(String rawLine) {
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
