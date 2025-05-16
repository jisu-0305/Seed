package org.example.backend.controller.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.project.enums.ServerStatus;

@Getter
@Builder
@AllArgsConstructor
public class ProjectAutoDeploymentStatusResponse {
    private ServerStatus serverStatus;
    private boolean isServerLive;
}
