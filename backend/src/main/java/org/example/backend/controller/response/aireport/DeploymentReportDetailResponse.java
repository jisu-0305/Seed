package org.example.backend.controller.response.aireport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.backend.domain.aireport.entity.AIDeploymentReport;
import org.example.backend.domain.aireport.enums.ReportStatus;

import java.util.List;

@Data
@Builder
public class DeploymentReportDetailResponse {
    private String title;
    private String summary;
    private List<String> files;
    private ReportStatus status;
    private String detail;
    private String commitUrl;
    private String mergeRequestUrl;

    public static DeploymentReportDetailResponse from(AIDeploymentReport report, List<String> files) {
        return DeploymentReportDetailResponse.builder()
                .title(report.getTitle())
                .summary(report.getSummary())
                .files(files)
                .status(report.getStatus())
                .detail(report.getAdditionalNotes())
                .commitUrl(report.getCommitUrl())
                .mergeRequestUrl(
                        report.getStatus() == ReportStatus.SUCCESS ? report.getMergeRequestUrl() : null
                )
                .build();
    }
}
