package org.example.backend.controller.request.docker;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "ContainerLogFilterRequest", description = "로그 조회 필터 옵션")
public record DockerContainerLogRequest(

        @Min(0)
        String tail, // 가져올 마지막 라인 수 (숫자 또는 all)

        Boolean stdout, // 표준 출력 포함 여부
        Boolean stderr, // 표준 에러 포함 여부
        Long since, // 조회 시작 시각 (Unix epoch seconds)
        Long until, //조회 종료 시각 (Unix epoch seconds)
        Boolean timestamps, // 타임스탬프 포함 여부
        Boolean details, // 메타정보 포함 여부(details)
        Boolean follow // 실시간 스트리밍 모드(follow)

) {
    public DockerContainerLogRequest {
        stdout     = stdout == null || stdout;
        stderr     = stderr == null || stderr;
        tail       = (tail == null || tail.isBlank()) ? "all" : tail;
        timestamps = timestamps != null && timestamps;
        details    = details != null && details;
        follow     = follow != null && follow;
    }
}
