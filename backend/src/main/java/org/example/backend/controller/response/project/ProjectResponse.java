package org.example.backend.controller.response.project;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.userproject.dto.UserInProject;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProjectResponse {
    private Long id;
    private String projectName;
    private LocalDateTime createdAt;
    private Long gitlabProjectId;
    private List<UserInProject> memberList;
    private boolean autoDeploymentEnabled;
    private boolean httpsEnabled;
    private BuildStatus buildStatus;
    private LocalDateTime lastBuildAt;
}
