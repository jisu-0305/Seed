package org.example.backend.domain.server.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.enums.ServerStatus;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServerStatusServiceImpl implements ServerStatusService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Project project, ServerStatus serverStatus) {
        project.updateAutoDeploymentStatus(serverStatus);
        projectRepository.saveAndFlush(project);
    }
}
