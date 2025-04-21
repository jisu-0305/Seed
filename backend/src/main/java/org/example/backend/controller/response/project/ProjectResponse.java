package org.example.backend.controller.response.project;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProjectResponse {
    private Long id;
    private String projectName;
    private LocalDateTime createdAt;

    private ProjectResponse(Long id, String projectName, LocalDateTime createdAt) {
        this.id = id;
        this.projectName = projectName;
        this.createdAt = createdAt;
    }

    public static ProjectResponse from(Long id, String projectName, LocalDateTime createdAt) {
        return new ProjectResponse(id, projectName, createdAt);
    }
}
