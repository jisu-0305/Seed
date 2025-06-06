package org.example.backend.domain.server.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.auth.ProjectAccessValidator;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.response.log.HttpsLogResponse;
import org.example.backend.domain.server.entity.HttpsLog;
import org.example.backend.domain.server.repository.HttpsLogRepository;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HttpsLogServiceImpl implements HttpsLogService{

    private final HttpsLogRepository httpsLogRepository;
    private final ProjectAccessValidator projectAccessValidator;

    @Override
    public void saveLog(Long projectId, String stepName, String logContent, String status) {
        httpsLogRepository.save(HttpsLog.builder()
                .projectId(projectId)
                .stepName(stepName)
                .logContent(logContent)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    public List<HttpsLogResponse> getLogs(Long projectId, String accessToken) {
        projectAccessValidator.validateUserInProject(projectId, accessToken);
        List<HttpsLog> logs = httpsLogRepository.findAllByProjectIdOrderByCreatedAtAsc(projectId);

        List<HttpsLogResponse> response = new ArrayList<>();
        int stepNumber = 1;
        for (HttpsLog log : logs) {
            response.add(HttpsLogResponse.builder()
                    .stepNumber(stepNumber++)
                    .stepName(log.getStepName())
                    .logContent(log.getLogContent())
                    .status(log.getStatus())
                    .createdAt(log.getCreatedAt())
                    .build());
        }

        return response;
    }

    @Override
    public List<HttpsLog> getLogsByProjectId(Long projectId) {
        return httpsLogRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
    }
}
