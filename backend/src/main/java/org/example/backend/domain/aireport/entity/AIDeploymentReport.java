package org.example.backend.domain.aireport.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.controller.request.DeploymentReportSavedRequest;
import org.example.backend.domain.aireport.enums.ReportStatus;
import org.example.backend.util.aiapi.dto.aireport.AIReportResponse;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ai_deployment_reports")
public class AIDeploymentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private int buildNumber;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    private String commitUrl;

    private String mergeRequestUrl;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private LocalDateTime createdAt;

    public static AIDeploymentReport fromAiReportResponse(Long projectId, int buildNumber,String title, AIReportResponse response, String commitUrl, String mergeRequestUrl, ReportStatus status) {
        return AIDeploymentReport.builder()
                .projectId(projectId)
                .buildNumber(buildNumber)
                .title(title)
                .summary(response.getSummary())
                .additionalNotes(response.getAdditionalNotes())
                .commitUrl(commitUrl)
                .mergeRequestUrl(mergeRequestUrl)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static AIDeploymentReport fromBackofficeRequest(DeploymentReportSavedRequest request) {
        return AIDeploymentReport.builder()
                .projectId(request.getProjectId())
                .buildNumber(request.getBuildNumber())
                .title(request.getTitle())
                .summary(request.getSummary())
                .additionalNotes(request.getAdditionalNotes())
                .commitUrl(request.getCommitUrl())
                .mergeRequestUrl(request.getMergeRequestUrl())
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .build();
    }
}