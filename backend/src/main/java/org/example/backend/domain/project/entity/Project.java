package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.project.enums.ServerStatus;
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
    @Column(length = 50)
    private ServerStatus serverStatus;

    @Enumerated(EnumType.STRING)
    private ProjectStructure structure;

    private String domainName;

    // mono
    private String gitlabTargetBranchName;
    private String frontendDirectoryName;
    private String backendDirectoryName;

    // multi
    private String frontendBranchName;
    private String backendBranchName;

    // config

    private String frontendFramework;
    private String nodejsVersion;

    private String jdkVersion;
    private String jdkBuildTool;

    private boolean autoDeploymentEnabled;
    private boolean httpsEnabled;

    private LocalDateTime lastBuildAt;

    @Enumerated(EnumType.STRING)
    private BuildStatus buildStatus;

    public void saveDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void enableAutoDeployment() {
        this.autoDeploymentEnabled = true;
    }

    public void enableHttps() {
        this.httpsEnabled = true;
    }

    public void initAutoDeploymentStatus() {
        this.serverStatus = ServerStatus.INIT;
    }

    public void updateAutoDeploymentStatus(ServerStatus status) {
        this.serverStatus = status;
    }

    public void updateBuildStatus(BuildStatus status) {
        this.buildStatus = status;
        this.lastBuildAt = LocalDateTime.now();
    }

    public void updateServerIP(String newServerIP) {
        this.serverIP = newServerIP;
    }

}

