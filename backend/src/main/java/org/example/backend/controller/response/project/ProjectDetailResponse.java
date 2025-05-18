package org.example.backend.controller.response.project;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.project.enums.ProjectStructure;
import org.example.backend.domain.project.enums.ServerStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProjectDetailResponse {
    private Long id;
    private Long ownerId;
    private String projectName;
    private String serverIP;
    private LocalDateTime createdAt;
    private String repositoryUrl;
    private ServerStatus serverStatus;
    private ProjectStructure structure;

    private String frontendDirectoryName;
    private String backendDirectoryName;
    private String frontendBranchName;
    private String backendBranchName;
    private String gitlabTargetBranchName;

    private String nodejsVersion;
    private String frontendFramework;
    private String frontendEnvFilePath;
    private String jdkVersion;
    private String jdkBuildTool;
    private String backendEnvFilePath;

    private List<ApplicationResponse> applicationList;
    private String pemFilePath;
}
