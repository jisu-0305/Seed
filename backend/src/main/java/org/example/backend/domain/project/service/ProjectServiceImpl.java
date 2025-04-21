package org.example.backend.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.response.project.ProjectResponse;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    public ProjectResponse createProject(ProjectCreateRequest request) {
        Project project = Project.create(request.getProjectName());
        Project saved = projectRepository.save(project);
        return toDto(saved);
    }

    @Override
    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        return toDto(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProjectResponse toDto(Project project) {
        return ProjectResponse.from(
                project.getId(),
                project.getProjectName(),
                project.getCreatedAt()
        );
    }
}
