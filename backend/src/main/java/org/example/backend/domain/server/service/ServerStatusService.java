package org.example.backend.domain.server.service;

import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.enums.ServerStatus;

public interface ServerStatusService {

    void updateStatus(Project project, ServerStatus serverStatus);

    void saveDomiaName(Project project, String domain);
}
