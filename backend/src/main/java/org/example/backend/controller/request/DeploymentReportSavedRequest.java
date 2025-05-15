package org.example.backend.controller.request;

import lombok.Data;
import org.example.backend.domain.aireport.enums.ReportStatus;

import java.util.List;

@Data
public class DeploymentReportSavedRequest {
    private Long projectId;
    private Long buildNumber;
    private String title;
    private String summary;
    private String additionalNotes;
    private String commitUrl;
    private String mergeRequestUrl;
    private ReportStatus status;
    private List<String> appliedFileNames;
}

