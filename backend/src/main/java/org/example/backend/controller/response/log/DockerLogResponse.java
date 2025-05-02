package org.example.backend.controller.response.log;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DockerLogResponse {
    private String logs;
}
