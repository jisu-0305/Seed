package org.example.backend.controller.response.project;

import lombok.*;
import org.example.backend.domain.project.enums.BuildStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatusResponse {

    private Long id;
    private String projectName;
    private boolean httpsEnabled;
    private boolean autoDeployEnabled;
    private BuildStatus lastBuildStatus;
    private LocalDateTime lastBuildAt;
}
