package org.example.backend.domain.project.service;

import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.response.project.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectCreateRequest request, String accessToken);
    ProjectResponse getProject(Long projectId, String accessToken);
    List<ProjectResponse> getAllProjects(String accessToken);
    void deleteProject(Long projectId, String accessToken);
}
