package org.example.backend.domain.aireport.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.auth.ProjectAccessValidator;
import org.example.backend.controller.request.DeploymentReportSavedRequest;
import org.example.backend.controller.response.aireport.DeploymentReportDetailResponse;
import org.example.backend.controller.response.aireport.DeploymentReportResponse;
import org.example.backend.domain.aireport.entity.AIDeploymentReport;
import org.example.backend.domain.aireport.entity.AppliedFile;
import org.example.backend.domain.aireport.repository.AIDeploymentReportRepository;
import org.example.backend.domain.aireport.repository.AppliedFileRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIDeploymentReportServiceImpl implements AIDeploymentReportService {

    private final AppliedFileRepository appliedFileRepository;
    private final AIDeploymentReportRepository reportRepository;
    private final ProjectAccessValidator projectProjectAccessValidator;

    @Override
    public List<DeploymentReportResponse> getReportList(Long projectId, String accessToken) {
        projectProjectAccessValidator.validateUserInProject(projectId, accessToken);
        return reportRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(aiDeploymentReport -> new DeploymentReportResponse(aiDeploymentReport.getId(), aiDeploymentReport.getTitle(), aiDeploymentReport.getStatus(), aiDeploymentReport.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public DeploymentReportDetailResponse getReportDetail(Long reportId, String accessToken) {
        AIDeploymentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        projectProjectAccessValidator.validateUserInProject(report.getProjectId(), accessToken);
        List<String> files = appliedFileRepository.findAllByAiReportId(reportId)
                .stream()
                .map(AppliedFile::getFileName)
                .collect(Collectors.toList());

        return DeploymentReportDetailResponse.from(report, files);
    }

    @Override
    public long saveReport(DeploymentReportSavedRequest request) {
        AIDeploymentReport report = AIDeploymentReport.fromBackofficeRequest(request);
        AIDeploymentReport saved = reportRepository.save(report);

        List<AppliedFile> files = request.getAppliedFileNames().stream()
                .map(fileName -> new AppliedFile(fileName, saved.getId()))
                .toList();

        appliedFileRepository.saveAll(files);

        return saved.getId();
    }
}
