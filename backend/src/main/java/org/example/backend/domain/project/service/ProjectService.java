package org.example.backend.domain.project.service;

import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.request.project.ProjectUpdateRequest;
import org.example.backend.controller.response.project.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectCreateRequest request, MultipartFile clientEnvFile, MultipartFile serverEnvFile, MultipartFile pemFile, String accessToken);
    ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, MultipartFile clientEnvFile, MultipartFile serverEnvFile, String accessToken);
    ProjectDetailResponse getProjectDetail(Long projectId, String accessToken);
    List<ProjectResponse> getAllProjects(String accessToken);
    void deleteProject(Long projectId, String accessToken);
    void markHttpsConverted(Long projectId);
    List<ProjectStatusResponse> getMyProjectStatuses(String accessToken);
    List<ProjectExecutionGroupResponse> getMyProjectExecutionsGroupedByDate(String accessToken);
    List<ProjectApplicationResponse> searchAvailableApplications(String accessToken, String keyword);
    ProjectAutoDeploymentStatusResponse getProjectAutoDeploymentStatus(Long projectId);
}
