package org.example.backend.controller.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProjectApplicationResponse {
    private String imageName;
    private Integer defaultPort;
}
