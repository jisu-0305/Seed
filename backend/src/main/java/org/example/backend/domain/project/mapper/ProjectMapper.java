package org.example.backend.domain.project.mapper;

import org.example.backend.controller.response.project.ProjectResponse;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.userproject.dto.UserInProject;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectMapper {

    public static ProjectResponse toResponse(Project project,
                                             List<UserInProject> memberList,
                                             boolean autoDeploymentEnabled,
                                             boolean httpsEnabled,
                                             BuildStatus buildStatus,
                                             LocalDateTime lastBuildAt) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .createdAt(project.getCreatedAt())
                .gitlabProjectId(project.getGitlabProjectId())
                .memberList(memberList)
                .autoDeploymentEnabled(autoDeploymentEnabled)
                .httpsEnabled(httpsEnabled)
                .buildStatus(buildStatus)
                .lastBuildAt(lastBuildAt)
                .build();
    }

}
