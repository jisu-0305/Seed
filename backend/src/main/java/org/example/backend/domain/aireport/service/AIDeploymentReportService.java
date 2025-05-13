package org.example.backend.domain.aireport.service;

import org.example.backend.controller.response.aireport.DeploymentReportDetailResponse;
import org.example.backend.controller.response.aireport.DeploymentReportResponse;
import org.example.backend.domain.aireport.entity.AIDeploymentReport;

import java.util.List;

public interface AIDeploymentReportService {
    List<DeploymentReportResponse> getReportList(Long projectId);
    DeploymentReportDetailResponse getReportDetail(Long reportId);
    AIDeploymentReport saveReport(AIDeploymentReport report, List<String> appliedFileNames);
}
