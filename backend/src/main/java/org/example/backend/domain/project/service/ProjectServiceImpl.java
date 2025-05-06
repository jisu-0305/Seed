package org.example.backend.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.response.project.*;
import org.example.backend.domain.project.entity.*;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.project.enums.ExecutionType;
import org.example.backend.domain.project.mapper.ProjectMapper;
import org.example.backend.domain.project.repository.*;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.domain.userproject.dto.UserInProject;
import org.example.backend.domain.userproject.entity.UserProject;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectConfigRepository projectConfigRepository;
    private final ApplicationRepository applicationRepository;
    private final RedisSessionManager redisSessionManager;
    private final UserProjectRepository userProjectRepository;
    private final ProjectExecutionRepository projectExecutionRepository;
    private final ProjectStatusRepository projectStatusRepository;
    private final UserRepository userRepository;

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

        String frontendEnvPath = saveFile(clientEnvFile, "env/client");
        String backendEnvPath = saveFile(serverEnvFile, "env/server");
        String pemPath = saveFile(pemFile, "pem");

        Project project = Project.builder()
                .ownerId(userId)
                .projectName(projectName)
                .serverIP(request.getServerIP())
                .repositoryUrl(request.getRepositoryUrl())
                .createdAt(LocalDateTime.now())
                .structure(request.getStructure())
                .frontendBranchName(request.getFrontendBranchName())
                .frontendDirectoryName(request.getFrontendDirectoryName())
                .backendBranchName(request.getBackendBranchName())
                .backendDirectoryName(request.getBackendDirectoryName())
                .pemFilePath(pemPath)
                .build();

        Project savedProject = projectRepository.save(project);

        userProjectRepository.save(UserProject.create(savedProject.getId(), userId));

        ProjectStatus status = ProjectStatus.builder()
                .projectId(savedProject.getId())
                .autoDeploymentEnabled(true)
                .httpsEnabled(false)
                .build();
        projectStatusRepository.save(status);

        projectConfigRepository.save(ProjectConfig.builder()
                .projectId(savedProject.getId())
                .nodejsVersion(request.getNodejsVersion())
                .frontendFramework(request.getFrontendFramework())
                .frontendEnvFile(frontendEnvPath)
                .jdkVersion(request.getJdkVersion())
                .jdkBuildTool(request.getJdkBuildTool())
                .backendEnvFile(backendEnvPath)
                .build());
        request.getApplicationList().forEach(app ->
                applicationRepository.save(Application.builder()
                        .imageName(app.getImageName())
                        .tag(app.getTag())
                        .port(app.getPort())
                        .projectId(savedProject.getId())
                        .build())
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UserInProject> memberList = List.of(UserInProject.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .userIdentifyId(user.getUserIdentifyId())
                .profileImageUrl(user.getProfileImageUrl())
                .build());

        return ProjectMapper.toResponse(
                savedProject,
                memberList,
                status.isAutoDeploymentEnabled(),
                status.isHttpsEnabled(),
                null,
                null
        );
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

        ProjectConfig config = projectConfigRepository.findByProjectId(projectId).orElse(null);
        List<Application> applications = applicationRepository.findAllByProjectId(projectId);

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .ownerId(project.getOwnerId())
                .projectName(project.getProjectName())
                .serverIP(project.getServerIP())
                .createdAt(project.getCreatedAt())
                .repositoryUrl(project.getRepositoryUrl())
                .structure(project.getStructure())
                .frontendBranchName(project.getFrontendBranchName())
                .frontendDirectoryName(project.getFrontendDirectoryName())
                .backendBranchName(project.getBackendBranchName())
                .backendDirectoryName(project.getBackendDirectoryName())
                .nodejsVersion(config != null ? config.getNodejsVersion() : null)
                .frontendFramework(config != null ? config.getFrontendFramework() : null)
                .frontendEnvFilePath(config != null ? config.getFrontendEnvFile() : null)
                .jdkVersion(config != null ? config.getJdkVersion() : null)
                .jdkBuildTool(config != null ? config.getJdkBuildTool() : null)
                .backendEnvFilePath(config != null ? config.getBackendEnvFile() : null)
                .applicationList(applications.stream()
                        .map(app -> ApplicationResponse.builder()
                                .imageName(app.getImageName())
                                .tag(app.getTag())
                                .port(app.getPort())
                                .build())
                        .toList())
                .pemFilePath(project.getPemFilePath())
                .build();
    }




    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        List<Long> projectIdList = userProjectRepository.findByUserId(userId).stream()
                .map(UserProject::getProjectId)
                .toList();

        Map<Long, Project> projectMap = projectRepository.findAllById(projectIdList).stream()
                .collect(Collectors.toMap(Project::getId, p -> p));

        Map<Long, ProjectStatus> statusMap = projectStatusRepository.findByProjectIdIn(projectIdList).stream()
                .collect(Collectors.toMap(ProjectStatus::getProjectId, s -> s));

        List<UserProject> allUserProjectList = userProjectRepository.findByProjectIdIn(projectIdList);
        List<Long> allUserIdList = allUserProjectList.stream().map(UserProject::getUserId).distinct().toList();

        Map<Long, User> userMap = userRepository.findAllById(allUserIdList).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, List<UserInProject>> projectUsersMap = allUserProjectList.stream()
                .collect(Collectors.groupingBy(
                        UserProject::getProjectId,
                        Collectors.mapping(up -> {
                            User user = userMap.get(up.getUserId());
                            return UserInProject.builder()
                                    .userId(user.getId())
                                    .userName(user.getUserName())
                                    .userIdentifyId(user.getUserIdentifyId())
                                    .profileImageUrl(user.getProfileImageUrl())
                                    .build();
                        }, Collectors.toList())
                ));

        return projectIdList.stream()
                .map(id -> {
                    Project project = projectMap.get(id);
                    ProjectStatus status = statusMap.get(id);
                    List<UserInProject> memberList = projectUsersMap.getOrDefault(id, List.of());

                    return ProjectMapper.toResponse(
                            project,
                            memberList,
                            status.isAutoDeploymentEnabled(),
                            status.isHttpsEnabled(),
                            status.getBuildStatus(),
                            status.getLastBuildAt()
                    );
                })
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

    @Override
    @Transactional
    public void markHttpsConverted(Long projectId) {
        ProjectStatus status = projectStatusRepository.findByProjectId(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_STATUS_NOT_FOUND));

        status.enableHttps();

        projectExecutionRepository.save(ProjectExecution.builder()
                .projectId(projectId)
                .executionType(ExecutionType.HTTPS)
                .projectExecutionTitle("HTTPS 설정")
                .executionStatus(BuildStatus.SUCCESS)
                .createdAt(LocalDate.now())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectStatusResponse> getMyProjectStatuses(String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        List<UserProject> userProjectList = userProjectRepository.findByUserId(userId);
        List<Long> projectIdList = userProjectList.stream()
                .map(UserProject::getProjectId)
                .toList();

        Map<Long, Project> projectMap = projectRepository.findAllById(projectIdList).stream()
                .collect(Collectors.toMap(Project::getId, p -> p));

        List<ProjectStatus> statuses = projectStatusRepository.findByProjectIdIn(projectIdList);

        return statuses.stream()
                .map(status -> {
                    Project project = projectMap.get(status.getProjectId());
                    if (project == null) return null;

                    return ProjectStatusResponse.builder()
                            .id(project.getId())
                            .projectName(project.getProjectName())
                            .httpsEnabled(status.isHttpsEnabled())
                            .autoDeploymentEnabled(status.isAutoDeploymentEnabled())
                            .buildStatus(status.getBuildStatus())
                            .lastBuildAt(status.getLastBuildAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProjectExecutionGroupResponse> getMyProjectExecutionsGroupedByDate(String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        List<Long> projectIds = userProjectRepository.findByUserId(userId).stream()
                .map(UserProject::getProjectId)
                .toList();

        Map<Long, String> projectNameMap = projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Project::getProjectName));

        List<ProjectExecution> allExecutionList = projectExecutionRepository.findByProjectIdInOrderByCreatedAtDesc(projectIds);

        Map<LocalDate, List<ProjectExecutionResponse>> grouped = allExecutionList.stream()
                .map(exec -> ProjectExecutionResponse.builder()
                        .id(exec.getId())
                        .projectName(projectNameMap.get(exec.getProjectId()))
                        .executionType(exec.getExecutionType())
                        .projectExecutionTitle(exec.getProjectExecutionTitle())
                        .executionStatus(exec.getExecutionStatus())
                        .buildNumber(exec.getBuildNumber())
                        .createdAt(exec.getCreatedAt())
                        .build())
                .collect(Collectors.groupingBy(ProjectExecutionResponse::getCreatedAt, LinkedHashMap::new, Collectors.toList()));

        return grouped.entrySet().stream()
                .map(e -> new ProjectExecutionGroupResponse(e.getKey(), e.getValue()))
                .toList();
    }



    private String extractProjectNameFromUrl(String url) {
        if (url == null || !url.endsWith(".git")) return "unknown";
        String[] parts = url.split("/");
        return parts[parts.length - 1].replace(".git", "");
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
