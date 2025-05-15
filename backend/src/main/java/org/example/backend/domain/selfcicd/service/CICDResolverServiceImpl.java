package org.example.backend.domain.selfcicd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.auth.ProjectAccessValidator;
import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.DockerContainerLogResponse;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.jenkins.JenkinsBuildListResponse;
import org.example.backend.domain.aireport.enums.ReportStatus;
import org.example.backend.domain.aireport.service.AIDeploymentReportService;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.domain.jenkins.service.JenkinsService;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.entity.ProjectApplication;
import org.example.backend.domain.project.repository.ApplicationRepository;
import org.example.backend.domain.project.repository.ProjectApplicationRepository;
import org.example.backend.domain.project.repository.ProjectRepository;
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
    private final ApplicationRepository applicationRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ObjectMapper objectMapper;
    private final ProjectAccessValidator projectProjectAccessValidator;
    private final AIApiClient fastAIClient;

    //여기서 사용하는 accessToekn은 gitlabPersonalAccessToken임
    @Override
    public void handleSelfHealingCI(Long projectId, String accessToken) {
        // 1. 프로젝트 조회
        Project project = getProject(projectId);

        // 1-1. 마지막 Jenkins 빌드 정보 및 에러 로그 조회
        int buildNumber = getLastBuildInfo(projectId);
        String errorLog = getErrorLog(projectId, buildNumber);
//        System.out.println(">>>>>>>>>>>>>buildNumber"+buildNumber+", jenkins log: "+errorLog);

        // 1-2. 프로젝트에 포함된 앱 이름 목록 조회 -> 그 참고로 appName없을수도 아님 아마도? default로 쓰는건 projects안에 fronted_framework로 받아야함, 해당 내용에 docker이미지 이름이랑 동일할거임
        List<String> appNames = getProjectAppNames(project);
//        System.out.println(">>>>>>>>>>>>>appNames"+appNames.toString());
        // 1-3. Gitlab 최신 MR의 diff 정보 조회
        GitlabCompareResponse gitDiff = getGitDiff(project, accessToken);

        //현재 jenkins 관련된거 다 jwtToken안받게 메서드 설정해야함!
        // 1-4. AI API 호출하여 의심되는 앱 추론
        List<String> suspectedApps = inferSuspectedApps(appNames, gitDiff, errorLog);
//        System.out.println(">>>>>>>>>>>>>suspectedApps"+suspectedApps.toString());

        // 1-5. 의심 앱들의 GitLab 트리 정보 조회
        Map<String, List<GitlabTree>> appTrees = getGitTrees(suspectedApps, project, accessToken);
//        System.out.println(">>>>>>>>>>>>>appTrees"+appTrees.toString());

//        // 1-6. 의심 앱들의 Docker 로그 수집 및 변환
        Map<String, String> appLogs = getDockerLogs(project, suspectedApps, gitDiff);

//        // 1-6. 의심앱들에 jenkins log 추가
//        Map<String, String> appLogs = new HashMap<>();
//        for (String app : appNames) {
//            appLogs.put(app, errorLog); // 모든 앱에 동일한 Jenkins 로그 대입
//        }
//        System.out.println(">>>>>>>>>>>>>appLogs: " + appLogs);


        // 2. suspect 파일 추론 및 AI 자동 수정 파일 수집
        List<PatchedFile> patchedFiles = new ArrayList<>();
        List<ResolveErrorResponse> resolveResults = new ArrayList<>();
        for (String suspectApp : suspectedApps) {
            // 2-1 ~ 2-4: suspect file 추론 → 원본코드 수집 → 해결 요약 요청 → 수정 파일 요청
            resolveResults.addAll(
                    resolveFilesAndPatch(project, accessToken, gitDiff, appLogs.get(suspectApp), appTrees.get(suspectApp), patchedFiles)
            );
        }

        System.out.println(">>>>>>>>>>>>>resolveResults"+resolveResults);

        // 3-1. GitLab에 새로운 브랜치 생성 (ex. ai/fix/65)
        String newBranch = createFixBranch(project, buildNumber, accessToken);

        // 3-2. GitLab에 수정된 파일들 커밋
        commitPatchedFiles(project, accessToken, newBranch, patchedFiles, buildNumber);

//        // 3-3. Jenkins 빌드 트리거 (새 브랜치 기준)
//        triggerRebuild(projectId, accessToken, newBranch);
//
//        // 4. 빌드 결과 확인 → MR 생성 → AI 리포트 요청 및 저장
//        // 4-1. Jenkins 빌드 결과 상태 확인
//        ReportStatus reportStatus  = getBuildStatus(projectId, accessToken);
//
//        // 4-2. 빌드 성공 시 GitLab MR 생성
//        if (reportStatus == ReportStatus.SUCCESS) {
//            createMergeRequest(project, accessToken, newBranch);
//        }
//
//        // 4-3. AI 요약 보고서 생성 요청 및 수신
//        Map<String, AIReportResponse> reportResponses = createAIReports(resolveResults, suspectedApps);
//
//        // 4-4. 생성된 리포트 결과 저장 (DB 저장 등)
//        saveReports(projectId, reportResponses);
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
        List<String> appNames = new ArrayList<>(
                projectApplicationRepository.findAllByProjectId(project.getId()).stream()
                        .map(ProjectApplication::getImageName)
                        .filter(Objects::nonNull)
                        .toList()
        );

        appNames.add("spring");
        appNames.add(project.getFrontendFramework());

        return appNames;
    }

    // 1-3. Git diff 정보 가져오기
    private GitlabCompareResponse getGitDiff(Project project, String accessToken) {
        return gitlabService.fetchLatestMrDiff(accessToken, project.getGitlabProjectId()).block();
    }

    // 1-4. AI API 호출: 1~3의 재료주고 의심되는 애플리케이션 추론 요청
    private List<String> inferSuspectedApps(List<String> appNames, GitlabCompareResponse gitDiff, String errorLog) {
        InferAppRequest request = InferAppRequest.builder()
                .gitDiff(gitDiff.getDiffs())
                .jenkinsLog(errorLog)
                .applicationNames(appNames)
                .build();
        return fastAIClient.requestInferApplications(request);
    }

    /**
     * 1-5. 해당 어플리케이션들의 트리 구조 가져오기
     * 진행 내용: Map의 키에는 Docker Image name이 들어가야함, 또한 브렌치 이름 필드 만들고 받아오기(현재 BackendoriginalProjectBracnh, FrontoriginProjectBranch만 있음)
     *          따라서 모노, 레포에 따른 변경과 해당 내용에 따른 로직 변경 필요. 당장은 MonooriginalProjectBracnh가 추가되어야하고 해당 값을 받아오는게 필요)
     * 담당자: 강승엽
     * */
    private Map<String, List<GitlabTree>> getGitTrees(List<String> appNames, Project project, String accessToken) {
        Map<String, String> appToFolderMap = Map.of(
                "spring", "backend",
                "react", "frontend",
                "nodejs", "frontend",
                "vue", "frontend"
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
    private Map<String, String> getDockerLogs(Project project, List<String> appNames, GitlabCompareResponse gitDiff) {
        Instant commitInstant = gitDiff.getCommit().getCreatedAt().toInstant();
        long since = commitInstant.getEpochSecond();
        long until = Instant.now().getEpochSecond();

        // 2) 요청 DTO 생성
        DockerContainerLogRequest request = new DockerContainerLogRequest(since, until);
        Map<String, List<DockerContainerLogResponse>> dockerAppLogs = new HashMap<>();

        // 3) 앱별로 로그 수집
        for (String app : appNames) {
            List<DockerContainerLogResponse> logs = dockerService.getContainerLogs(project.getServerIP(), app, request);
            dockerAppLogs.put(app, logs);
        }

        return dockerAppLogs.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(log -> {
                                    if (log.timestamp() != null) {
                                        return log.timestamp() + " " + log.message();
                                    } else {
                                        return log.message();
                                    }
                                })
                                .collect(Collectors.joining("\n"))
                ));
    }

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
            throw new BusinessException(ErrorCode.AI_INFER_REQUEST_FAILED);
        }

        // 2-2. suspect 파일 찾기 요청
        SuspectFileRequest suspectRequest = SuspectFileRequest.builder()
                .diffRaw(diffJson)
                .tree(treeJson)
                .log(appLog)
                .build();

        SuspectFileInnerResponse suspectFilesResponse = fastAIClient.requestSuspectFiles(suspectRequest).getResponse();

        // 2-3. suspect 파일들의 원본 코드 GitLab에서 조회
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
        String fileRawJson;
        try {
            fileRawJson = objectMapper.writeValueAsString(filesRaw);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_RESOLVE_REQUEST_FAILED);
        }

        ResolveErrorResponse resolveDto = fastAIClient
                .requestResolveError(suspectFilesResponse, fileRawJson);

        // 2-5. 해결 요약본 기반으로 수정된 코드 요청
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

            PatchedFile patchedFile = fastAIClient.requestPatchFile(patchRequest);
            patchedFilesCollector.add(patchedFile);  // 외부에서 주입받은 리스트에 추가
        }

        result.add(resolveDto);
        return result;
    }

    //3-1. GitLab에 새로운 브랜치 생성, 브렌치이름 date에 시간 분까지 나오면 좋을듯?
    private String createFixBranch(Project project, int buildNumber, String accessToken) {
        String branchName = "ai/fix/" + buildNumber;
        gitlabService.createBranch(
                accessToken,
                project.getGitlabProjectId(),
                branchName,
                project.getGitlabTargetBranchName()
        );
        return branchName;
    }

    // 3-2. GitLab에 AI를 통해 수정된 파일들 커밋
    private void commitPatchedFiles(Project project, String accessToken, String branchName, List<PatchedFile> patchedFiles, int BuildNumber) {
        if (patchedFiles == null || patchedFiles.isEmpty()) return;
        String commitMessage = "refactor: jenkins 빌드 번호 - "+BuildNumber+" 수정한 커밋입니다.";

        gitlabService.commitPatchedFiles(
                accessToken,
                project.getGitlabProjectId(),
                branchName,
                commitMessage,
                patchedFiles
        );
    }

    /**
     * 3-3. Jenkins에 해당 브렌치로 재빌드 요청
     * 내용: branchName파라미터 아직 없음 추가해줘야함
     * 담당자: 강승엽
     *
     * 내용2: branchName에 아까 3-1에 생성한 name으로 넣어주면 됨
     * 담당자: 김지수
     */
    private void triggerRebuild(Long projectId, String accessToken, String branchName) {
//        jenkinsService.triggerBuild(projectId, accessToken, branchName);
    }

    // 4-1. 마지막 Jenkins 빌드 상태 조회
    private ReportStatus getBuildStatus(Long projectId, String accessToken) {
        return ReportStatus.fromJenkinsStatus(jenkinsService.getLastBuild(projectId, accessToken).getStatus());
    }

    // 4-2. 빌드 성공 시 GitLab에 Merge Request 생성
    private String createMergeRequest(Project project, String accessToken, String branchName) {
        return gitlabService.createMergeRequest(
                accessToken,
                project.getGitlabProjectId(),
                branchName,
                project.getGitlabTargetBranchName(),
                "[AI 수정 제안] 빌드 자동 복구",
                "AI가 수정한 코드를 기반으로 빌드가 성공했습니다. 검토 후 병합해주세요."
        ).getWebUrl();
    }

    // 4-3. AI리포트 요청 및 응답 결과 매핑
    private Map<String, AIReportResponse> createAIReports(
            List<ResolveErrorResponse> resolveResults,
            List<String> suspectedApps
    ) {
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

            AIReportResponse response = fastAIClient.requestErrorReport(request);
            reports.put(appName, response);
        }

        return reports;
    }

    // 4-4. 리포트 DB 저장 (AIDeploymentReportServiceImpl 사용)
    private void saveReports(Long projectId, Map<String, AIReportResponse> reportResponses) {
//        for (Map.Entry<String, AIReportResponse> entry : reportResponses.entrySet()) {
//            String appName = entry.getKey();
//            AIReportResponse report = entry.getValue();
//
//            DeploymentReportSavedRequest saveRequest = DeploymentReportSavedRequest.builder()
//                    .projectId(projectId)
//                    .title("[AI 수정 제안] " + appName)
//                    .summary(report.getResolutionReport().getErrorSummary())
//                    .addtionNotes(
//                            "원인: " + report.getResolutionReport().getCause() + "\n" +
//                                    "해결: " + report.getResolutionReport().getFinalResolution()
//                    )
//                    .status("SUCCESS") // 또는 "FAIL" 등 동적으로 설정 가능
//                    .commitUrl(report.getCommitUrl())
//                    .mergeRequestUrl(report.getMergeRequestUrl())
//                    .appliedFileNames(
//                            report.getFileFixes().stream().map(FileFix::getPath).toList()
//                    )
//                    .build();
//
//            aiDeploymentReportService.saveReport(saveRequest); // 실제 저장
//        }
    }
}

