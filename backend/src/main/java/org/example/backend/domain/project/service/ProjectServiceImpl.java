package org.example.backend.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.response.project.ApplicationResponse;
import org.example.backend.controller.response.project.ProjectDetailResponse;
import org.example.backend.controller.response.project.ProjectResponse;
import org.example.backend.domain.project.entity.Application;
import org.example.backend.domain.project.entity.EnvironmentConfig;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.entity.ProjectStructureDetails;
import org.example.backend.domain.project.enums.PlatformType;
import org.example.backend.domain.project.mapper.ProjectMapper;
import org.example.backend.domain.project.repository.ApplicationRepository;
import org.example.backend.domain.project.repository.EnvironmentConfigRepository;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.domain.project.repository.ProjectStructureDetailsRepository;
import org.example.backend.domain.userproject.entity.UserProject;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.backend.domain.project.mapper.ProjectMapper.*;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectStructureDetailsRepository structureDetailsRepository;
    private final EnvironmentConfigRepository environmentConfigRepository;
    private final ApplicationRepository applicationRepository;
    private final RedisSessionManager redisSessionManager;
    private final UserProjectRepository userProjectRepository;

    @Value("${file.base-path}")
    private String basePath;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request,
                                         MultipartFile clientEnvFile,
                                         MultipartFile serverEnvFile,
                                         MultipartFile pemFile,
                                         String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        String projectName = extractProjectNameFromUrl(request.getRepositoryUrl());

        String clientEnvPath = saveFile(clientEnvFile, "env/client");
        String serverEnvPath = saveFile(serverEnvFile, "env/server");
        String pemPath = saveFile(pemFile, "pem");

        Project project = Project.builder()
                .ownerId(userId)
                .projectName(projectName)
                .repositoryUrl(request.getRepositoryUrl())
                .ipAddress(request.getIpAddress())
                .pemFilePath(pemPath)
                .createdAt(LocalDateTime.now())
                .build();

        Project savedProject = projectRepository.save(project);
        userProjectRepository.save(UserProject.create(savedProject.getId(), userId));

        structureDetailsRepository.save(createStructureDetails(request, savedProject.getId()));

        saveEnvironmentConfig(savedProject.getId(), PlatformType.CLIENT, request.getClientNodeVersion(), clientEnvPath, null);
        saveEnvironmentConfig(savedProject.getId(), PlatformType.SERVER, request.getServerJdkVersion(), serverEnvPath, request.getServerBuildTool());

        request.getApplications().forEach(app ->
                applicationRepository.save(Application.builder()
                        .name(app.getName())
                        .tag(app.getTag())
                        .port(app.getPort())
                        .projectId(savedProject.getId())
                        .build())
        );

        return ProjectMapper.toResponse(savedProject);
    }

    @Override
    public ProjectDetailResponse getProjectDetail(Long projectId, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        if (!userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        ProjectStructureDetails structure = structureDetailsRepository.findByProjectId(projectId).orElse(null);
        EnvironmentConfig clientEnv = environmentConfigRepository.findByProjectIdAndPlatformType(projectId, PlatformType.CLIENT).orElse(null);
        EnvironmentConfig serverEnv = environmentConfigRepository.findByProjectIdAndPlatformType(projectId, PlatformType.SERVER).orElse(null);
        List<Application> applications = applicationRepository.findAllByProjectId(projectId);

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .repositoryUrl(project.getRepositoryUrl())
                .ipAddress(project.getIpAddress())
                .pemFilePath(project.getPemFilePath())
                .structure(structure != null ? structure.getStructure() : null)
                .clientDirectoryName(structure != null ? structure.getClientDirectoryName() : null)
                .serverDirectoryName(structure != null ? structure.getServerDirectoryName() : null)
                .clientBranchName(structure != null ? structure.getClientBranchName() : null)
                .serverBranchName(structure != null ? structure.getServerBranchName() : null)
                .clientNodeVersion(clientEnv != null ? clientEnv.getVersion() : null)
                .clientEnvFilePath(clientEnv != null ? clientEnv.getEnvFileName() : null)
                .serverJdkVersion(serverEnv != null ? serverEnv.getVersion() : null)
                .serverEnvFilePath(serverEnv != null ? serverEnv.getEnvFileName() : null)
                .serverBuildTool(serverEnv != null ? serverEnv.getBuildTool() : null)
                .applications(applications.stream()
                        .map(app -> ApplicationResponse.builder()
                                .name(app.getName())
                                .tag(app.getTag())
                                .port(app.getPort())
                                .build())
                        .toList())
                .build();
    }




    @Override
    public List<ProjectResponse> getAllProjects(String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        List<Long> projectIds = userProjectRepository.findByUserId(userId)
                .stream()
                .map(UserProject::getProjectId)
                .toList();

        return projectRepository.findAllById(projectIds).stream()
                .map(ProjectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        if (!userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getOwnerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        userProjectRepository.deleteAllByProjectId(projectId);
        projectRepository.delete(project);
    }

    private String extractProjectNameFromUrl(String url) {
        if (url == null || !url.endsWith(".git")) return "unknown";
        String[] parts = url.split("/");
        return parts[parts.length - 1].replace(".git", "");
    }

    private ProjectStructureDetails createStructureDetails(ProjectCreateRequest request, Long projectId) {
        return ProjectStructureDetails.builder()
                .structure(request.getStructure())
                .clientDirectoryName(request.getClientDirectoryName())
                .serverDirectoryName(request.getServerDirectoryName())
                .clientBranchName(request.getClientBranchName())
                .serverBranchName(request.getServerBranchName())
                .projectId(projectId)
                .build();
    }

    private void saveEnvironmentConfig(Long projectId, PlatformType type, String version, String envFileName, String buildTool) {
        EnvironmentConfig config = EnvironmentConfig.builder()
                .platformType(type)
                .version(version)
                .envFileName(envFileName)
                .buildTool(buildTool)
                .projectId(projectId)
                .build();
        environmentConfigRepository.save(config);
    }

    private String saveFile(MultipartFile file, String subPath) {
        if (file == null || file.isEmpty()) return null;

        try {
            Path dirPath = Paths.get(basePath).toAbsolutePath().resolve(subPath);
            Files.createDirectories(dirPath);

            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("uploaded-file");
            Path fullPath = dirPath.resolve(fileName);

            file.transferTo(fullPath.toFile());

            return fullPath.toString();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }




}
