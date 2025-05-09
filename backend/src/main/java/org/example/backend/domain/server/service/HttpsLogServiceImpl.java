package org.example.backend.domain.server.service;

import lombok.RequiredArgsConstructor;
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
    private final RedisSessionManager redisSessionManager;
    private final UserProjectRepository userProjectRepository;

    @Override
    public void saveLog(Long projectId, String stepName, String logContent) {
        httpsLogRepository.save(HttpsLog.builder()
                .projectId(projectId)
                .stepName(stepName)
                .logContent(logContent)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public List<HttpsLogResponse> getLogs(Long projectId, String accessToken) {
        validateUserInProject(projectId, accessToken);
        List<HttpsLog> logs = httpsLogRepository.findAllByProjectIdOrderByCreatedAtAsc(projectId);

        List<HttpsLogResponse> response = new ArrayList<>();
        int stepNumber = 1;
        for (HttpsLog log : logs) {
            response.add(HttpsLogResponse.builder()
                    .stepNumber(stepNumber++)
                    .stepName(log.getStepName())
                    .logContent(log.getLogContent())
                    .createdAt(log.getCreatedAt())
                    .build());
        }

        return response;
    }

    @Override
    public List<HttpsLog> getLogsByProjectId(Long projectId) {
        return httpsLogRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
    }

    private void validateUserInProject(Long projectId, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();
        if (!userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }
    }
}
