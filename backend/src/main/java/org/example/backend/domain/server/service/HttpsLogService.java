package org.example.backend.domain.server.service;

import org.example.backend.controller.response.log.HttpsLogResponse;
import org.example.backend.domain.server.entity.HttpsLog;

import java.util.List;

public interface HttpsLogService {
    void saveLog(Long projectId, String stepName, String logContent);
    List<HttpsLogResponse> getLogs(Long projectId, String accessToken);
    List<HttpsLog> getLogsByProjectId(Long projectId);
}
