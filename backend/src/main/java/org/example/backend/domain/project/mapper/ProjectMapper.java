package org.example.backend.domain.project.mapper;

import org.example.backend.controller.response.project.ProjectResponse;
import org.example.backend.domain.project.entity.Project;

public class ProjectMapper {
    public static ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
