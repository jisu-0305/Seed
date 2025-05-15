package org.example.backend.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.request.project.ProjectUpdateRequest;
import org.example.backend.controller.response.project.*;
import org.example.backend.domain.project.entity.*;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.project.enums.ExecutionType;
import org.example.backend.domain.project.enums.FileType;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final RedisSessionManager redisSessionManager;
    private final UserProjectRepository userProjectRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectExecutionRepository projectExecutionRepository;
    private final UserRepository userRepository;
    private final ProjectFileRepository projectFileRepository;

    @Value("${file.base-path}")
    private String basePath;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, MultipartFile frontEnvFile,
                                         MultipartFile backendEnvFile, MultipartFile pemFile, String accessToken) {

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        String projectName = extractProjectNameFromUrl(request.getRepositoryUrl());

        Project project = Project.builder()
                .ownerId(userId)
                .projectName(projectName)
                .serverIP(request.getServerIP())
                .repositoryUrl(request.getRepositoryUrl())
                .createdAt(LocalDateTime.now())
                .gitlabProjectId(request.getGitlabProjectId())
                .structure(request.getStructure())
                .gitlabTargetBranchName(request.getGitlabTargetBranch())
                .frontendDirectoryName(request.getFrontendDirectoryName())
                .backendDirectoryName(request.getBackendDirectoryName())
                .nodejsVersion(request.getNodejsVersion())
                .frontendFramework(request.getFrontendFramework())
                .jdkVersion(request.getJdkVersion())
                .jdkBuildTool(request.getJdkBuildTool())
                .autoDeploymentEnabled(false)
                .httpsEnabled(false)
                .build();

        projectRepository.save(project);

        try {
            ProjectFile newFrontendEnvFile = ProjectFile.builder()
                    .projectId(project.getId())
                    .fileName(frontEnvFile.getOriginalFilename())
                    .fileType(FileType.FRONTEND_ENV)
                    .contentType(frontEnvFile.getContentType())
                    .data(frontEnvFile.getBytes())
                    .build();

            ProjectFile newBackendEnvFile = ProjectFile.builder()
                    .projectId(project.getId())
                    .fileName(backendEnvFile.getOriginalFilename())
                    .fileType(FileType.BACKEND_ENV)
                    .contentType(backendEnvFile.getContentType())
                    .data(backendEnvFile.getBytes())
                    .build();

            ProjectFile newPemFile = ProjectFile.builder()
                    .projectId(project.getId())
                    .fileName(pemFile.getOriginalFilename())
                    .fileType(FileType.PEM)
                    .contentType(pemFile.getContentType())
                    .data(pemFile.getBytes())
                    .build();

            projectFileRepository.save(newFrontendEnvFile);
            projectFileRepository.save(newBackendEnvFile);
            projectFileRepository.save(newPemFile);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }

        userProjectRepository.save(UserProject.create(project.getId(), userId));

        if (request.getApplicationList() != null && !request.getApplicationList().isEmpty()) {

            List<ProjectApplication> updateApplicatinList = request.getApplicationList().stream()
                    .map(applicationRequest -> {
                        Application master = applicationRepository
                                .findFirstByImageNameOrderByIdAsc(applicationRequest.getImageName())
                                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

                        return ProjectApplication.builder()
                                .projectId(project.getId())
                                .applicationId(master.getId())
                                .imageName(applicationRequest.getImageName())
                                .tag(applicationRequest.getTag())
                                .port(applicationRequest.getPort())
                                .build();
                    })
                    .toList();

            projectApplicationRepository.saveAll(updateApplicatinList);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UserInProject> memberList = List.of(UserInProject.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .userIdentifyId(user.getUserIdentifyId())
                .profileImageUrl(user.getProfileImageUrl())
                .build());

        return ProjectMapper.toResponse(
                project,
                memberList,
                project.isAutoDeploymentEnabled(),
                project.isHttpsEnabled(),
                null,
                null
        );
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request,
                                         MultipartFile clientEnvFile, MultipartFile serverEnvFile, String accessToken) {

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        if (!userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        if (request.getServerIP() != null && !request.getServerIP().isBlank()) {
            project.updateServerIP(request.getServerIP());
        }

        try {
            if (clientEnvFile != null && !clientEnvFile.isEmpty()) {
                upsertEnvFile(projectId, clientEnvFile, FileType.FRONTEND_ENV);
            }
            if (serverEnvFile != null && !serverEnvFile.isEmpty()) {
                upsertEnvFile(projectId, serverEnvFile, FileType.BACKEND_ENV);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }

        if (request.getApplications() != null && !request.getApplications().isEmpty()) {
            projectApplicationRepository.deleteAllByProjectId(projectId);

            List<ProjectApplication> updateApplicatinList = request.getApplications().stream()
                    .map(applicationRequest -> {
                        Application master = applicationRepository
                                .findFirstByImageNameOrderByIdAsc(applicationRequest.getImageName())
                                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

                        return ProjectApplication.builder()
                                .projectId(projectId)
                                .applicationId(master.getId())
                                .imageName(applicationRequest.getImageName())
                                .tag(applicationRequest.getTag())
                                .port(applicationRequest.getPort())
                                .build();
                    })
                    .toList();

            projectApplicationRepository.saveAll(updateApplicatinList);
        } else {
            projectApplicationRepository.deleteAllByProjectId(projectId);
        }

        List<UserInProject> memberList = userProjectRepository.findByProjectId(projectId).stream()
                .map(userProject -> {
                    User user = userRepository.findById(userProject.getUserId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                    return UserInProject.builder()
                            .userId(user.getId())
                            .userName(user.getUserName())
                            .userIdentifyId(user.getUserIdentifyId())
                            .profileImageUrl(user.getProfileImageUrl())
                            .build();
                }).toList();

        return ProjectMapper.toResponse(
                project,
                memberList,
                project.isAutoDeploymentEnabled(),
                project.isHttpsEnabled(),
                project.getBuildStatus(),
                project.getLastBuildAt()
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

        List<ProjectApplication> projectApplicationList = projectApplicationRepository.findAllByProjectId(projectId);

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .ownerId(project.getOwnerId())
                .projectName(project.getProjectName())
                .serverIP(project.getServerIP())
                .createdAt(project.getCreatedAt())
                .repositoryUrl(project.getRepositoryUrl())
                .structure(project.getStructure())
                .gitlabTargetBranchName(project.getGitlabTargetBranchName())
                .frontendDirectoryName(project.getFrontendDirectoryName())
                .backendDirectoryName(project.getBackendDirectoryName())
                .nodejsVersion(project.getNodejsVersion())
                .frontendFramework(project.getFrontendFramework())
                .jdkVersion(project.getJdkVersion())
                .jdkBuildTool(project.getJdkBuildTool())
                .applicationList(projectApplicationList.stream()
                        .map(app -> ApplicationResponse.builder()
                                .applicationId(app.getApplicationId())
                                .imageName(app.getImageName())
                                .tag(app.getTag())
                                .port(app.getPort())
                                .build())
                        .toList())
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

        Map<Long, Project> projectMap = projectRepository.findByIdIn(projectIdList).stream()
                .collect(Collectors.toMap(Project::getId, p -> p));

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
                    List<UserInProject> memberList = projectUsersMap.getOrDefault(id, List.of());

                    return ProjectMapper.toResponse(
                            project,
                            memberList,
                            project.isAutoDeploymentEnabled(),
                            project.isHttpsEnabled(),
                            project.getBuildStatus(),
                            project.getLastBuildAt()
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
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_STATUS_NOT_FOUND));

        project.enableHttps();

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

        List<Project> projectList = projectRepository.findByIdIn(projectIdList);

        return projectList.stream()
                .map(project -> {

                    if (project == null) return null;

                    return ProjectStatusResponse.builder()
                            .id(project.getId())
                            .projectName(project.getProjectName())
                            .httpsEnabled(project.isHttpsEnabled())
                            .autoDeploymentEnabled(project.isAutoDeploymentEnabled())
                            .buildStatus(project.getBuildStatus())
                            .lastBuildAt(project.getLastBuildAt())
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

    @Override
    @Transactional(readOnly = true)
    public List<ProjectApplicationResponse> searchAvailableApplications(String accessToken, String keyword) {

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<Application> found = applicationRepository.findByImageNameContainingIgnoreCase(keyword);

        return found.stream()
                .collect(Collectors.groupingBy(Application::getImageName))
                .entrySet().stream()
                .map(entry -> {
                    String imageName = entry.getKey();
                    List<Application> apps = entry.getValue();

                    Application minApp = apps.stream()
                            .min(Comparator.comparingLong(Application::getId))
                            .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

                    List<Integer> defaultPorts = apps.stream()
                            .map(Application::getDefaultPort)
                            .toList();

                    return ProjectApplicationResponse.builder()
                            .applicationId(minApp.getId())
                            .imageName(imageName)
                            .description(minApp.getDescription())
                            .defaultPorts(defaultPorts)
                            .build();
                })
                .toList();

    }

    private void upsertEnvFile(Long projectId, MultipartFile file, FileType type) throws IOException {
        byte[] data = file.getBytes();
        String name = file.getOriginalFilename();
        String contentType = file.getContentType();

        projectFileRepository.findByProjectIdAndFileType(projectId, type)
                .ifPresentOrElse(existing -> {
                    existing.updateProjectEnvFile(name, contentType, data);
                    projectFileRepository.save(existing);
                }, () -> {
                    ProjectFile pf = ProjectFile.builder()
                            .projectId(projectId)
                            .fileName(name)
                            .fileType(type)
                            .contentType(contentType)
                            .data(data)
                            .build();
                    projectFileRepository.save(pf);
                });
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
