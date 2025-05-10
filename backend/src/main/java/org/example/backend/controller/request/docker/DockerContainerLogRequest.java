package org.example.backend.controller.request.docker;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "ContainerLogFilterRequest", description = "로그 조회 필터 옵션")
public record DockerContainerLogRequest(

        @Min(0)
        String tailLines, // 가져올 마지막 라인 수 (숫자 또는 all)

        Boolean includeStdout, // 표준 출력 포함 여부
        Boolean includeStderr, // 표준 에러 포함 여부
        Long sinceSeconds, // 조회 시작 시각 (Unix epoch seconds)
        Long untilSeconds, //조회 종료 시각 (Unix epoch seconds)
        Boolean includeTimestamps, // 타임스탬프 포함 여부
        Boolean includeDetails, // 메타정보 포함 여부(includeDetails)
        Boolean followStream // 실시간 스트리밍 모드(followStream)

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
