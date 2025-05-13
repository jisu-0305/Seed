package org.example.backend.domain.aireport.service;

import lombok.RequiredArgsConstructor;
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

    private final AIDeploymentReportRepository reportRepository;
    private final AppliedFileRepository appliedFileRepository;

    @Override
    public List<DeploymentReportResponse> getReportList(Long projectId) {
        return reportRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(r -> new DeploymentReportResponse(r.getId(), r.getTitle(), r.getStatus(), r.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public DeploymentReportDetailResponse getReportDetail(Long reportId) {
        AIDeploymentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        List<String> files = appliedFileRepository.findAllByAiReportId(reportId)
                .stream()
                .map(AppliedFile::getFileName)
                .collect(Collectors.toList());

        return DeploymentReportDetailResponse.from(report, files);
    }

    @Override
    public AIDeploymentReport saveReport(AIDeploymentReport report, List<String> appliedFileNames) {
        AIDeploymentReport saved = reportRepository.save(report);
        List<AppliedFile> files = appliedFileNames.stream()
                .map(name -> new AppliedFile(name, saved.getId()))
                .collect(Collectors.toList());
        appliedFileRepository.saveAll(files);
        return saved;
    }
}
