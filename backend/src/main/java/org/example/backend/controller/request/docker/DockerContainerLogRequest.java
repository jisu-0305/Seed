package org.example.backend.controller.request.docker;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "ContainerLogFilterRequest", description = "로그 조회 필터 옵션")
public record DockerContainerLogRequest(

        @Min(0)
        String tailLines,

        Boolean includeStdout,
        Boolean includeStderr,
        Long sinceSeconds,
        Long untilSeconds,
        Boolean includeTimestamps,
        Boolean includeDetails,
        Boolean followStream

) {
    public DockerContainerLogRequest {
        includeStdout = includeStdout == null || includeStdout;
        includeStderr = includeStderr == null || includeStderr;
        tailLines = (tailLines == null || tailLines.isBlank()) ? "all" : tailLines;
        includeTimestamps = includeTimestamps != null && includeTimestamps;
        includeDetails = includeDetails != null && includeDetails;
        followStream = followStream != null && followStream;
    }
}
