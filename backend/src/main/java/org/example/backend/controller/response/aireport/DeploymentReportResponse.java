package org.example.backend.controller.response.aireport;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend.domain.aireport.enums.ReportStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DeploymentReportResponse {
    private Long id;
    private Long buildNumber;
    private String title;
    private ReportStatus status;
    private LocalDateTime date;
}
