package org.example.backend.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.response.project.ProjectResponse;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.mapper.ProjectMapper;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.domain.userproject.entity.UserProject;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.example.backend.domain.project.mapper.ProjectMapper.*;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final RedisSessionManager redisSessionManager;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        Project project = Project.create(request.getProjectName());
        Project savedProject = projectRepository.save(project);

        UserProject userProject = UserProject.create(savedProject.getId(), userId);
        userProjectRepository.save(userProject);

        return toResponse(savedProject);
    }

    @Override
    public ProjectResponse getProject(Long projectId, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        if (userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_USER_PROJECT);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        return toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects(String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        List<UserProject> mappings = userProjectRepository.findByUserId(userId);
        List<Long> projectIdList = mappings.stream()
                .map(UserProject::getProjectId)
                .toList();

        return projectRepository.findAllById(projectIdList).stream()
                .map(ProjectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        if (userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_USER_PROJECT);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        projectRepository.delete(project);
    }
}
