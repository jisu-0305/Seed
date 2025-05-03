package org.example.backend.controller.request.project;

import lombok.Getter;
import org.example.backend.domain.project.enums.ProjectStructure;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class ProjectCreateRequest {
    private String serverIP;
    private String repositoryUrl;
    private ProjectStructure structure;
    private String frontendBranchName;
    private String frontendDirectoryName;
    private String backendBranchName;
    private String backendDirectoryName;
    private String nodejsVersion;
    private String frontendFramework;
    private String jdkVersion;
    private String jdkBuildTool;
    private List<ApplicationRequest> applicationList;
}
