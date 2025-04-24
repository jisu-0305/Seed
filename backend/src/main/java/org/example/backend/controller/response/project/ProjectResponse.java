package org.example.backend.controller.response.project;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectResponse {
    private Long id;
    private String projectName;
    private String repositoryUrl;
    private String ipAddress;
    private String pemFilePath;
}
