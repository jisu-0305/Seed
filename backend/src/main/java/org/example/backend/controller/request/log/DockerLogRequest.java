package org.example.backend.controller.request.log;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DockerLogRequest {
    private String ip;
    private String pemPath;
    private String containerName;
    private String since; // ex) "5m" or ISO time like "2025-04-23T15:00:00"
}

