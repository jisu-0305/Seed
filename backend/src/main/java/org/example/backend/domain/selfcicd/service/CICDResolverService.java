package org.example.backend.domain.selfcicd.service;

import org.example.backend.controller.request.log.DockerLogRequest;
import org.example.backend.controller.response.log.DockerLogResponse;

public interface CICDResolverService {
    DockerLogResponse getRecentDockerLogs(DockerLogRequest request);

    void handleSelfHealingCI(Long projectId, String accessToken);
}
