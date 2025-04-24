package org.example.backend.domain.project.service;

import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.response.project.ProjectDetailResponse;
import org.example.backend.controller.response.project.ProjectResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectCreateRequest request, MultipartFile clientEnvFile, MultipartFile serverEnvFile, MultipartFile pemFile, String accessToken);
    ProjectDetailResponse getProjectDetail(Long projectId, String accessToken);
    List<ProjectResponse> getAllProjects(String accessToken);
    void deleteProject(Long projectId, String accessToken);
}
