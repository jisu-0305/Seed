package org.example.backend.domain.selfcicd.service;

import org.example.backend.controller.request.log.DockerLogRequest;
import org.example.backend.controller.response.log.DockerLogResponse;

public interface SelfCICDService {
    DockerLogResponse getRecentDockerLogs(DockerLogRequest request);
}

