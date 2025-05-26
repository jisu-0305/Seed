package org.example.backend.controller.request;

import lombok.Data;
import org.example.backend.domain.aireport.enums.ReportStatus;

import java.util.List;
import java.util.Set;

@Data
public class DeploymentReportSavedRequest {
    private Long projectId;
    private int buildNumber;
    private String title;
    private String summary;
    private String additionalNotes;
    private String commitUrl;
    private String mergeRequestUrl;
    private ReportStatus status;
    private Set<String> appliedFileNames;
}

