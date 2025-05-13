package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.project.enums.ProjectStructure;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long gitlabProjectId;
    private Long ownerId;
    private String projectName;
    private String serverIP;
    private LocalDateTime createdAt;
    private String repositoryUrl;

    @Enumerated(EnumType.STRING)
    private ProjectStructure structure;

    // mono
    private String gitlabTargetBranchName;
    private String frontendDirectoryName;
    private String backendDirectoryName;

    // multi
    private String frontendBranchName;
    private String backendBranchName;

    // config
    private String pemFilePath;

    private String frontendFramework;
    private String nodejsVersion;
    private String frontendEnvFilePath;

    private String jdkVersion;
    private String jdkBuildTool;
    private String backendEnvFilePath;

    private boolean autoDeploymentEnabled;
    private boolean httpsEnabled;

    private LocalDateTime lastBuildAt;

    @Enumerated(EnumType.STRING)
    private BuildStatus buildStatus;

    public void enableHttps() {
        this.httpsEnabled = true;
    }

    public void updateBuildStatus(BuildStatus status) {
        this.buildStatus = status;
        this.lastBuildAt = LocalDateTime.now();
    }

    public void update(String pemFilePath, String frontendEnvFilePath, String backendEnvFilePath) {
        this.pemFilePath = pemFilePath;
        this.frontendEnvFilePath = frontendEnvFilePath;
        this.backendEnvFilePath = backendEnvFilePath;
    }
}

