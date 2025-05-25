package org.example.backend.domain.selfcicd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.PolyUpperBound;
import org.example.backend.controller.request.DeploymentReportSavedRequest;
import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.DockerContainerLogResponse;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.domain.aireport.enums.ReportStatus;
import org.example.backend.domain.aireport.service.AIDeploymentReportService;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.domain.fcm.service.NotificationService;
import org.example.backend.domain.fcm.template.NotificationMessageTemplate;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.domain.jenkins.service.JenkinsService;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.enums.ServerStatus;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.domain.server.service.ServerStatusService;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.util.aiapi.AIApiClient;
import org.example.backend.util.aiapi.dto.aireport.AIReportRequest;
import org.example.backend.util.aiapi.dto.aireport.AIReportResponse;
import org.example.backend.util.aiapi.dto.patchfile.PatchFileRequest;
import org.example.backend.util.aiapi.dto.resolvefile.FileFix;
import org.example.backend.util.aiapi.dto.resolvefile.ResolutionReport;
import org.example.backend.util.aiapi.dto.resolvefile.ResolveErrorResponse;
import org.example.backend.util.aiapi.dto.suspectapp.InferAppRequest;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileInnerResponse;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CICDResolverServiceImpl implements CICDResolverService {

    private final JenkinsService jenkinsService;
    private final DockerService dockerService;
    private final GitlabService gitlabService;
    private final ProjectRepository projectRepository;
    private final AIDeploymentReportService aiDeploymentReportService;
    private final ObjectMapper objectMapper;
    private final AIApiClient fastAIClient;
    private final NotificationService notificationService;
    private final ServerStatusService serverStatusService;

    @Override
    public void handleSelfHealingCI(Long projectId, String accessToken, String failType) {
        Project project = getProject(projectId);

        try {
            // 1. sleep 5초 걸기
            waitBeforeStart(project);

            // 1-1. 마지막 Jenkins 빌드 정보 및 에러 로그 조회
            serverStatusService.updateStatus(project, ServerStatus.JENKINS_BUILD_LOG);
            int buildNumber = getLastBuildInfo(projectId);
            String jenkinsErrorLog = getErrorLog(projectId, buildNumber);
            log.warn(">>>>>>>>1-1호출완료, 1-2 호출 예정");

            // 1-2. 프로젝트에 포함된 앱 이름 목록 조회
            serverStatusService.updateStatus(project, ServerStatus.COLLECTING_APP_INFO);
            List<String> appNames = getProjectAppNames(project);

            // 1-3. Gitlab 최신 MR의 diff 정보 조회
            GitlabCompareResponse gitDiff = getGitDiff(project, accessToken);

            // 1-4. AI API 호출하여 의심되는 앱 추론
            serverStatusService.updateStatus(project, ServerStatus.INFERING_ERROR_SOURCE);
            List<String> suspectedApps = inferSuspectedApps(appNames, gitDiff, jenkinsErrorLog, projectId);

            // 1-5. 의심 앱들의 GitLab 트리 정보 조회
            serverStatusService.updateStatus(project, ServerStatus.COLLECTING_LOGS_AND_TREES);
            Map<String, List<GitlabTree>> appTrees = getGitTrees(suspectedApps, project, accessToken);

            // 1-6. 의심 앱들의 Docker 로그 수집 및 변환
            Map<String, String> appLogs = getAppLogs(project, suspectedApps, gitDiff, failType, jenkinsErrorLog);
            log.warn(">>>>>>>>1-6호출완료, 2-1 호출 예정");

            // 2. suspect 파일 추론 및 AI 자동 수정 파일 수집
            List<PatchedFile> patchedFiles = new ArrayList<>();
            List<ResolveErrorResponse> resolveResults = new ArrayList<>();
            for (String suspectApp : suspectedApps) {
                // 2-1 ~ 2-4: suspect file 추론 → 원본코드 수집 → 해결 요약 요청 → 수정 파일 요청
                resolveResults.addAll(
                        resolveFilesAndPatch(project, accessToken, gitDiff, appLogs.get(suspectApp), appTrees.get(suspectApp), patchedFiles)
                );
            }
            log.warn(">>>>>>>>2-4 호출완료, 3-1 호출 예정");

            int newBuildNumber = buildNumber + 1;
            // 3-1. GitLab에 새로운 브랜치 생성 (ex. seed/fix/65)
            serverStatusService.updateStatus(project, ServerStatus.COMMITTING_FIXES);
            String newBranch = createFixBranch(project, newBuildNumber, accessToken);

            // 3-2. GitLab에 수정된 파일들 커밋
            String commitUrl = commitPatchedFiles(project, accessToken, newBranch, patchedFiles, newBuildNumber);

            // 3-3. Jenkins 빌드 트리거 (새 브랜치 기준)
            serverStatusService.updateStatus(project, ServerStatus.JENKINS_REBUILDING);
            triggerRebuild(projectId, newBranch, project.getGitlabTargetBranchName());
            log.warn(">>>>>>>>3-3호출완료, 4-1 호출 예정");

            // 4. 빌드 결과 확인 → MR 생성 → AI 리포트 요청 및 저장
            // 4-1. Jenkins 빌드 결과 상태 확인
            ReportStatus reportStatus  = getBuildStatus(newBuildNumber, projectId);

            // 4-2. AI 요약 보고서 생성 요청 및 수신
            serverStatusService.updateStatus(project, ServerStatus.CREATING_REPORT);
            Map<String, AIReportResponse> reportResponses = createAIReports(resolveResults, suspectedApps, projectId);

            // 4-3. 빌드 성공 시 GitLab MR 생성
            String mergeRequestUrl = "";
            if (reportStatus == ReportStatus.SUCCESS) {
                serverStatusService.updateStatus(project, ServerStatus.CREATE_PULL_REQUEST);
                mergeRequestUrl = createMergeRequest(project, accessToken, newBranch, reportResponses);

                // 빌드 성공 알림 보내기
                notificationService.notifyProjectStatusForUsers(
                        projectId,
                        NotificationMessageTemplate.CICD_BUILD_COMPLETED
                );
            }

            // 4-4. 생성된 리포트 결과 저장 (DB 저장 등)
            serverStatusService.updateStatus(project, ServerStatus.SAVING_REPORT);
            saveAIReports(projectId, reportResponses, reportStatus, commitUrl, mergeRequestUrl, newBuildNumber);

            if (reportStatus == ReportStatus.SUCCESS) {
                serverStatusService.updateStatus(project, ServerStatus.FINISH_WITH_AI);
            } else {
                serverStatusService.updateStatus(project, ServerStatus.FAIL_WTIH_AI);
            }
        } catch (Exception e) {
            serverStatusService.updateStatus(project, ServerStatus.FAIL_WTIH_AI);

            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    projectId,
                    ServerStatus.FAIL_WTIH_AI
            );
        }
    }

    // 0. 호출 API Jenkins build 시간에 맞춰 작동
    private void waitBeforeStart(Project project) {
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            serverStatusService.updateStatus(project, ServerStatus.BUILD_FAIL_WITH_AI);

            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, project.getId(), ServerStatus.BUILD_FAIL_WITH_AI);
        }
    }

    // 1. 프로젝트 조회
    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    // 1-1. 마지막 Jenkins 빌드 번호 반환
    private int getLastBuildInfo(Long projectId) {
        return jenkinsService.getLastBuildNumberWithOutLogin(projectId);
    }

    // 1-1. 실패한 Jenkins 로그 조회
    private String getErrorLog(Long projectId, int buildNumber) {
        return jenkinsService.getBuildLogWithOutLogin(buildNumber, projectId);
    }

    // 1-2. 해당 프로젝트의 어플리케이션 목록 조회
    private List<String> getProjectAppNames(Project project) {
//        List<String> appNames = new ArrayList<>(
//                projectApplicationRepository.findAllByProjectId(project.getId()).stream()
//                        .map(ProjectApplication::getImageName)
//                        .filter(Objects::nonNull)
//                        .toList()
//        );
        List<String> appNames = new ArrayList<>();

        appNames.add("spring");
        appNames.add(project.getFrontendFramework());

        return appNames;
    }

    // 1-3. Git diff 정보 가져오기
    private GitlabCompareResponse getGitDiff(Project project, String accessToken) {
        return gitlabService.fetchLatestMrDiff(accessToken, project).block();
    }

    // 1-4. AI API 호출: 1~3의 재료주고 의심되는 애플리케이션 추론 요청
    private List<String> inferSuspectedApps(List<String> appNames, GitlabCompareResponse gitDiff, String errorLog, Long projectId) {
        InferAppRequest request = InferAppRequest.builder()
                .gitDiff(gitDiff.getDiffs())
                .jenkinsLog(errorLog)
                .applicationNames(appNames)
                .build();
        return fastAIClient.requestInferApplications(request, projectId);
    }

     //1-5. 해당 어플리케이션들의 트리 구조 가져오기
    private Map<String, List<GitlabTree>> getGitTrees(List<String> appNames, Project project, String accessToken) {
        Map<String, String> appToFolderMap = Map.of(
                "spring", project.getBackendDirectoryName(),
                project.getFrontendFramework(), project.getFrontendDirectoryName()
        );

        Map<String, List<GitlabTree>> appTrees = new HashMap<>();
        for (String appName : appNames) {
            String folder = appToFolderMap.getOrDefault(appName, appName);
            List<GitlabTree> tree = gitlabService.getRepositoryTree(
                    accessToken,
                    project.getGitlabProjectId(),
                    folder,
                    true,
                    project.getGitlabTargetBranchName()
            );
            appTrees.put(appName, tree);
        }
        return appTrees;
    }

    // 1-6. 해당 어플리케이션들의 log가져오기
    private Map<String, String> getAppLogs(Project project, List<String> appNames, GitlabCompareResponse gitDiff, String failType, String jenkinsErrorLog) {
        Map<String, String> appLogs = new HashMap<>();

        // Docker 로그 요청을 위한 시간 범위 계산
        Instant commitInstant = gitDiff.getCommit().getCreatedAt().toInstant();
        long since = commitInstant.getEpochSecond();
        long until = Instant.now().getEpochSecond();
        DockerContainerLogRequest dockerContainerLogRequest = new DockerContainerLogRequest(since, until);

        for (String app : appNames) {
            if ("spring".equalsIgnoreCase(app) && failType.equals("RUNTIME")) {
                List<DockerContainerLogResponse> dockerContainerlogs =
                        dockerService.getContainerLogs(project.getServerIP(), app, dockerContainerLogRequest);

                String dockerLog = dockerContainerlogs.stream()
                        .map(log -> {
                            if (log.timestamp() != null) {
                                return log.timestamp() + " " + log.message();
                            } else {
                                return log.message();
                            }
                        })
                        .collect(Collectors.joining("\n"));

                appLogs.put(app, dockerLog.isEmpty() ? "Docker 로그 없음" : dockerLog);
            } else {
                appLogs.put(app, jenkinsErrorLog);
            }
        }

        return appLogs;
    }

    // 2. AI를 통한 파일 수정 로직
    private List<ResolveErrorResponse> resolveFilesAndPatch(
            Project project,
            String accessToken,
            GitlabCompareResponse gitDiff,
            String appLog,
            List<GitlabTree> tree,
            List<PatchedFile> patchedFilesCollector
    ) {
        List<ResolveErrorResponse> result = new ArrayList<>();

        // 2-1. suspect file 요청을 위한 diff, tree json 생성
        Map<String, Object> diffRawPayload = new HashMap<>();
        diffRawPayload.put("commit", Map.of(
                "title", "auto-generated commit",
                "message", "generated by simulateSelfHealing()"
        ));
        diffRawPayload.put("diffs", gitDiff.getDiffs());

        String diffJson;
        String treeJson;
        try {
            diffJson = objectMapper.writeValueAsString(diffRawPayload);
            treeJson = objectMapper.writeValueAsString(tree);
        } catch (JsonProcessingException e) {
            serverStatusService.updateStatus(project, ServerStatus.BUILD_FAIL_WITH_AI);
            throw new BusinessException(ErrorCode.AI_INFER_REQUEST_FAILED, project.getId(), ServerStatus.BUILD_FAIL_WITH_AI);
        }

        // 2-2. suspect 파일 찾기 요청
        serverStatusService.updateStatus(project, ServerStatus.SUSPECT_FILE);

        SuspectFileRequest suspectRequest = SuspectFileRequest.builder()
                .diffRaw(diffJson)
                .tree(treeJson)
                .log(appLog)
                .build();

        SuspectFileInnerResponse suspectFilesResponse = fastAIClient.requestSuspectFiles(suspectRequest, project.getId()).getResponse();

        // 2-3. suspect 파일들의 원본 코드 GitLab에서 조회
        serverStatusService.updateStatus(project, ServerStatus.GET_ORIGINAL_CODE);

        List<Map<String, String>> filesRaw = new ArrayList<>();
        for (var file : suspectFilesResponse.getSuspectFiles()) {
            String path = file.getPath();
            String code = gitlabService.getRawFileContent(
                    accessToken,
                    project.getGitlabProjectId(),
                    path,
                    "master"
            );
            filesRaw.add(Map.of("path", path, "code", code));
        }

        // 2-4. 해결 요약 요청
        serverStatusService.updateStatus(project, ServerStatus.GET_INSTRUCTION);

        String fileRawJson;
        try {
            fileRawJson = objectMapper.writeValueAsString(filesRaw);
        } catch (JsonProcessingException e) {
            serverStatusService.updateStatus(project, ServerStatus.BUILD_FAIL_WITH_AI);
            throw new BusinessException(ErrorCode.AI_RESOLVE_REQUEST_FAILED, project.getId(), ServerStatus.BUILD_FAIL_WITH_AI);
        }

        ResolveErrorResponse resolveDto = fastAIClient
                .requestResolveError(suspectFilesResponse, fileRawJson, project.getId());

        // 2-5. 해결 요약본 기반으로 수정된 코드 요청
        serverStatusService.updateStatus(project, ServerStatus.GET_FIXED_CODE);

        for (var fix : resolveDto.getResponse().getFileFixes()) {
            String path = fix.getPath();
            String instruction = fix.getInstruction();
            String originalCode = filesRaw.stream()
                    .filter(f -> f.get("path").equals(path))
                    .findFirst()
                    .map(f -> f.get("code"))
                    .orElse("");

            PatchFileRequest patchRequest = PatchFileRequest.builder()
                    .path(path)
                    .originalCode(originalCode)
                    .instruction(instruction)
                    .build();

            PatchedFile patchedFile = fastAIClient.requestPatchFile(patchRequest, project.getId());
            patchedFilesCollector.add(patchedFile);  // 외부에서 주입받은 리스트에 추가
        }

        result.add(resolveDto);
        return result;
    }

    //3-1. GitLab에 새로운 브랜치 생성, 브렌치이름 date에 시간 분까지 나오면 좋을듯?
    @Transactional
    public String createFixBranch(Project project, int newBuildNumber, String accessToken) {
        String branchName = "seed/fix/" + newBuildNumber;
        gitlabService.deleteBranch(accessToken, project.getGitlabProjectId(), branchName);
        gitlabService.createBranch(
                accessToken,
                project.getGitlabProjectId(),
                branchName,
                project.getGitlabTargetBranchName()
        );
        return branchName;
    }

    // 3-2. GitLab에 AI를 통해 수정된 파일들 커밋
    @Transactional
    public String commitPatchedFiles(Project project, String accessToken, String branchName, List<PatchedFile> patchedFiles, int newBuildNumber) {
        if (patchedFiles == null || patchedFiles.isEmpty()) throw new BusinessException(ErrorCode.GITLAB_BAD_CREATE_COMMIT);
        String commitMessage = "refactor: jenkins "+newBuildNumber+"번 빌드 AI가 CICDresolver 기능을 통해 수정 완료";

        return gitlabService.commitPatchedFiles(
                accessToken,
                project.getGitlabProjectId(),
                branchName,
                commitMessage,
                patchedFiles
        ).getWebUrl();
    }

    // 3-3. Jenkins에 해당 브렌치로 재빌드 요청
    @Transactional
    public void triggerRebuild(Long projectId, String branchName, String originalBranchName) {
        jenkinsService.triggerBuildWithOutLogin(projectId, branchName, originalBranchName);
    }

    // 4-1. 마지막 Jenkins 빌드 상태 조회
    private ReportStatus getBuildStatus(int newBuildNumber, Long projectId) {
        return jenkinsService.waitUntilBuildFinishes(newBuildNumber, projectId);
    }

    // 4-2. AI리포트 요청 및 응답 결과 매핑
    @Transactional
    public Map<String, AIReportResponse> createAIReports(List<ResolveErrorResponse> resolveResults, List<String> suspectedApps, Long projectId) {
        Map<String, AIReportResponse> reports = new HashMap<>();

        for (int i = 0; i < resolveResults.size(); i++) {
            ResolveErrorResponse resolveDto = resolveResults.get(i);
            String appName = suspectedApps.get(i);

            AIReportRequest request = AIReportRequest.builder()
                    .fileFixes(resolveDto.getResponse().getFileFixes().stream()
                            .map(fix -> FileFix.builder()
                                    .path(fix.getPath())
                                    .instruction(fix.getInstruction())
                                    .explanation(fix.getExplanation())
                                    .build())
                            .toList())
                    .resolutionReport(ResolutionReport.builder()
                            .errorSummary(resolveDto.getResponse().getResolutionReport().getErrorSummary())
                            .cause(resolveDto.getResponse().getResolutionReport().getCause())
                            .finalResolution(resolveDto.getResponse().getResolutionReport().getFinalResolution())
                            .build())
                    .build();

            AIReportResponse response = fastAIClient.requestErrorReport(request, projectId);
            reports.put(appName, response);
        }

        return reports;
    }

    // 4-3. 빌드 성공 시 GitLab에 Merge Request 생성
    @Transactional
    public String createMergeRequest(Project project, String accessToken, String branchName, Map<String, AIReportResponse> reportResponses) {
        String apps = String.join(", ",
                reportResponses.keySet()
                        .stream()
                        .sorted() // 알파벳 정렬 optional
                        .toList()
        );

        // 제목 구성
        String title = String.format("[%s] aifix: %s 어플리케이션 수정", branchName, apps);

        StringBuilder description = new StringBuilder("## 🧠 AI 수정 요약\n\n");

        reportResponses.forEach((app, report) -> {
            description.append("### 🔧 앱: ").append(app).append("\n");
            description.append("- 요약: ").append(report.getSummary()).append("\n");
            description.append("- 원인: ").append(report.getAdditionalNotes()).append("\n\n");
        });

        return gitlabService.createMergeRequest(
                accessToken,
                project.getGitlabProjectId(),
                branchName,
                project.getGitlabTargetBranchName(),
                title,
                description.toString()
        ).getWebUrl();
    }

    // 4-4. 리포트 DB 저장
    @Transactional
    public void saveAIReports(Long projectId, Map<String, AIReportResponse> reportResponses, ReportStatus status, String commitUrl, String mergeRequestUrl, int newBuildNumber) {
        StringBuilder summaryBuilder = new StringBuilder();
        StringBuilder notesBuilder = new StringBuilder();
        List<String> mergedFiles = new ArrayList<>();

        for (Map.Entry<String, AIReportResponse> entry : reportResponses.entrySet()) {
            String appName = entry.getKey();
            AIReportResponse response = entry.getValue();

            summaryBuilder.append("[").append(appName).append("]\n")
                    .append(response.getSummary()).append("\n\n");

            notesBuilder.append("[").append(appName).append("]\n")
                    .append(response.getAdditionalNotes()).append("\n\n");

            mergedFiles.addAll(response.getAppliedFiles());
        }

        DeploymentReportSavedRequest request = new DeploymentReportSavedRequest();
        request.setProjectId(projectId);
        request.setBuildNumber(newBuildNumber);
        request.setTitle("["+(newBuildNumber-1) +"번 빌드 수정] AI 수정 보고서");
        request.setSummary(summaryBuilder.toString().trim());
        request.setAdditionalNotes(notesBuilder.toString().trim());
        request.setCommitUrl(commitUrl);
        request.setMergeRequestUrl(status == ReportStatus.SUCCESS ? mergeRequestUrl : null);
        request.setStatus(status);
        request.setAppliedFileNames(mergedFiles);

        aiDeploymentReportService.saveReport(request);
    }
}
