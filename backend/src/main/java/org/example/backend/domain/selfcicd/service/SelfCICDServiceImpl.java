package org.example.backend.domain.selfcicd.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.log.DockerLogRequest;
import org.example.backend.controller.response.log.DockerLogResponse;
import org.example.backend.util.log.LogUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SelfCICDServiceImpl implements SelfCICDService {

    @Override
    public DockerLogResponse getRecentDockerLogs(DockerLogRequest request) {
        String logs = LogUtil.getRecentDockerLogs(
                request.getIp(),
                request.getPemPath(),
                request.getContainerName(),
                request.getSince()
        );
        return new DockerLogResponse(logs);
    }
}

