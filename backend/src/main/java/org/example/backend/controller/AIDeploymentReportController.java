package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.response.aireport.DeploymentReportDetailResponse;
import org.example.backend.controller.response.aireport.DeploymentReportListResponse;
import org.example.backend.domain.aireport.service.AIDeploymentReportService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aireport")
@Tag(name = "AI Deployment Report", description = "AI 요약 보고서 관련 API")
public class AIDeploymentReportController {

    private final AIDeploymentReportService reportService;

    @Operation(summary = "프로젝트별 AI 보고서 목록 조회")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<DeploymentReportListResponse>> getReportList(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(new DeploymentReportListResponse(reportService.getReportList(projectId))));
    }

    @Operation(summary = "AI 보고서 상세 조회")
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<DeploymentReportDetailResponse>> getReportDetail(@PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportDetail(reportId)));
    }
}
