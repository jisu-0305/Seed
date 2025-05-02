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

    private List<UserInProject> members;
    private boolean autoDeployEnabled;
    private boolean httpsEnabled;
    private BuildStatus lastBuildStatus;
    private LocalDateTime lastBuildAt;
}
