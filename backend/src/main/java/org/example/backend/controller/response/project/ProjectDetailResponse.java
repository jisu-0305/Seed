package org.example.backend.controller.response.project;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.project.enums.ProjectStructure;

import java.util.List;

@Getter
@Builder
public class ProjectDetailResponse {
    private Long id;
    private String projectName;
    private String repositoryUrl;
    private String ipAddress;
    private String pemFilePath;
    private ProjectStructure structure;
    private String clientDirectoryName;
    private String serverDirectoryName;
    private String clientBranchName;
    private String serverBranchName;
    private String clientNodeVersion;
    private String clientEnvFilePath;
    private String serverJdkVersion;
    private String serverEnvFilePath;
    private String serverBuildTool;
    private List<ApplicationResponse> applications;
}
