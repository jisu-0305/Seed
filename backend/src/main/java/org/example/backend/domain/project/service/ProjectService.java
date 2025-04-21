package org.example.backend.domain.project.service;

import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.response.project.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectCreateRequest request);
    ProjectResponse getProject(Long id);
    List<ProjectResponse> getAllProjects();
}
