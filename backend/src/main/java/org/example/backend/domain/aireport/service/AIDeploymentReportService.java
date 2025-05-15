package org.example.backend.domain.aireport.service;

import org.example.backend.controller.request.DeploymentReportSavedRequest;
import org.example.backend.controller.response.aireport.DeploymentReportDetailResponse;
import org.example.backend.controller.response.aireport.DeploymentReportResponse;

import java.util.List;

public interface AIDeploymentReportService {
    List<DeploymentReportResponse> getReportList(Long projectId, String accessToken);
    DeploymentReportDetailResponse getReportDetail(Long reportId, String accessToken);
    long saveReport(DeploymentReportSavedRequest request);
}
