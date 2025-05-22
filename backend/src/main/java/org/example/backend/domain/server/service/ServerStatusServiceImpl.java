package org.example.backend.domain.server.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.enums.ServerStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServerStatusServiceImpl implements ServerStatusService {

    @Override
    @Transactional
    public void updateStatus(Project project, ServerStatus serverStatus) {
        if (ServerStatus.FINISH.equals(serverStatus)) {
            project.enableAutoDeployment();
        }

        project.updateAutoDeploymentStatus(serverStatus);
    }

    @Override
    @Transactional
    public void saveDomiaName(Project project, String domain) {
        project.saveDomainName(domain);
    }
}
