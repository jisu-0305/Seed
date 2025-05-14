package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.DeploymentReportSavedRequest;
import org.example.backend.controller.response.aireport.DeploymentReportDetailResponse;
import org.example.backend.controller.response.aireport.DeploymentReportListResponse;
import org.example.backend.domain.aireport.service.AIDeploymentReportService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-report")
@Tag(name = "AI Deployment Report", description = "AI 요약 보고서 관련 API")
public class AIDeploymentReportController {

    private final AIDeploymentReportService reportService;

    @Operation(summary = "프로젝트별 AI 보고서 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<DeploymentReportListResponse>> getReportList(
            @RequestParam Long projectId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                new DeploymentReportListResponse(reportService.getReportList(projectId, accessToken))
        ));
    }

    @Operation(summary = "AI 보고서 상세 조회")
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<DeploymentReportDetailResponse>> getReportDetail(
            @RequestParam Long reportId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportDetail(reportId, accessToken)));
    }

    @Operation(summary = "[BackOffice] AI 보고서 저장")
    @PostMapping("/backoffice/save")
    public ResponseEntity<ApiResponse<Long>> saveReportFromBackOffice(
            @RequestBody DeploymentReportSavedRequest request
    ) {
        long id = reportService.saveReport(request);
        return ResponseEntity.ok(ApiResponse.success(id));
    }
}
