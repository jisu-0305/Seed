package org.example.backend.controller.request.docker;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContainerLogFilterRequest", description = "로그 조회 필터 옵션")
public record DockerContainerLogRequest(
        Long sinceSeconds,
        Long untilSeconds
) {}
