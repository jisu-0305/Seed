package org.example.backend.domain.selfcicd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.request.log.DockerLogRequest;
import org.example.backend.controller.response.docker.DockerContainerLogResponse;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.jenkins.JenkinsBuildListResponse;
import org.example.backend.controller.response.log.DockerLogResponse;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.domain.jenkins.service.JenkinsService;
import org.example.backend.domain.project.entity.Application;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.entity.ProjectApplication;
import org.example.backend.domain.project.repository.ApplicationRepository;
import org.example.backend.domain.project.repository.ProjectApplicationRepository;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.util.aiapi.AIApiClient;
import org.example.backend.util.aiapi.dto.aireport.AIReportRequest;
import org.example.backend.util.aiapi.dto.aireport.ReportResponse;
import org.example.backend.util.aiapi.dto.patchfile.PatchFileRequest;
import org.example.backend.util.aiapi.dto.resolvefile.FileFix;
import org.example.backend.util.aiapi.dto.resolvefile.ResolutionReport;
import org.example.backend.util.aiapi.dto.resolvefile.ResolveErrorResponse;
import org.example.backend.util.aiapi.dto.suspectapp.InferAppRequest;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileInnerResponse;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileRequest;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileResponse;
import org.example.backend.util.log.LogUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CICDResolverServiceImpl implements CICDResolverService {
    private final JenkinsService jenkinsService;
    private final DockerService dockerService;
    private final AIApiClient fastAIClient;
    private final GitlabService gitlabService;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final ObjectMapper objectMapper;
    private final ProjectApplicationRepository projectApplicationRepository;

    @Override
    public DockerLogResponse getRecentDockerLogs(DockerLogRequest request) {
        String logs = LogUtil.getRecentDockerLogs(
                request.getIp(),
                request.getPemPath(),
                request.getContainerName(),
                request.getSince()
        );
        return new DockerLogResponse(logs);
    }

    //두번째 기능 메인 로직
    @Override
    public void handleSelfHealingCI(Long projectId, String accessToken) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 1-1. 실패한 Jenkins 로그 조회
        JenkinsBuildListResponse lastBuild = jenkinsService.getLastBuild(projectId, accessToken);
        int buildNumber = lastBuild.getBuildNumber();
        String jenkinsErrorLog = jenkinsService.getBuildLog(buildNumber, projectId, accessToken);

        // 1-2. 해당 프로젝트의 어플리케이션 목록 조회
        List<ProjectApplication> apps = projectApplicationRepository.findAllByProjectId(project.getId());
        List<String> appNames = apps.stream()
                .map(ProjectApplication::getImageName)
                .toList();

        // 1-3. Git diff 정보 가져오기 -> 관련해서 GitlabCompareCommit.id 필드가 실제 커밋 id인지 사실 여부 확인 필요 - 담당자: 박유진
        GitlabCompareResponse gitlabCompareResponse = gitlabService.fetchLatestMrDiff(accessToken, project.getId()).block();

        // 1-4. AI API 호출: 1~3의 재료주고 의심되는 애플리케이션 추론 요청
        InferAppRequest inferAppRequest = InferAppRequest.builder()
                .gitDiff(gitlabCompareResponse.getDiffs())
                .jenkinsLog(jenkinsErrorLog)
                .applicationNames(appNames)
                .build();

        List<String> suspectedApps= fastAIClient.requestInferApplications(inferAppRequest);
        log.debug(">>>>>>>>>>>>>의심 app 찾기"+suspectedApps.toString());

        /**
         * 1-5. 해당 어플리케이션들의 트리 구조 가져오기
         * 진행 내용: Map의 키에는 Docker Image name이 들어가야함, 또한 브렌치 이름 필드 만들고 받아오기(현재 BackendoriginalProjectBracnh, FrontoriginProjectBranch만 있음)
         *          따라서 모노, 레포에 따른 변경과 해당 내용에 따른 로직 변경 필요. 당장은 MonooriginalProjectBracnh가 추가되어야하고 해당 값을 받아오는게 필요)
         * 담당자: 강승엽
         * */
        Map<String, String> appToFolderMap = Map.of(
                "spring", "backend",
                "react", "frontend",
                "nodejs", "frontend",
                "vue", "frontend"
        );

        Map<String, List<GitlabTree>> appTrees = new HashMap<>();

        // 브렌치 이름 필요(임시로 master로 지정, 추후 모노 레포에 따른 브렌치 필드 추가시 변경)
//        String originalProjectBranch = project.getBackendoriginalProjectBranch();
        String originalProjectBranch = "master";

        for (String appName : suspectedApps) {
            String folder = appToFolderMap.getOrDefault(appName, appName);
            List<GitlabTree> tree = gitlabService.getRepositoryTree(
                    accessToken,
                    projectId,
                    folder, // path
                    true,   // recursive
                    originalProjectBranch
            );
            appTrees.put(appName, tree);
        }

        /**
         * 1-6. 해당 어플리케이션들의 log가져오기
         * 문제점: 현재 docker log는 app이름을 통해서 containerID구분해서 가져오는거같은데 문제는 여러 프로젝트의 경우 appName중복 가능성, 즉 파라미터로 projectID받아야하는거 아닌가 싶음)
         *        메서드 수정 필요! 추가로 DockerContainerLogRequest 주석으로는 해당내용 알 수가 없음 -> 노션에 설명 추가나 예시 필요.
         * 진행 내용: 추가로 해당 내용으로
         * 담당자: 박유진
         *
         * 문제점2: 현재 Project에는 backendDirectoryName, frontendDirectoryName 2가지 존재 결국 Nginx를 수정해야한다면? 관련된 코드는 어디서.
         *         추가로 log가져올때도 docker log 사용해야할거같은데 nginx는 별도의 log찾아야하는거 아닌가 싶음(결국 서버 접속 필요...? -> keypem저장?)
         * 담당자: 엔티티 - 강승엽, nginx 소스코드 저장 - 이재훈
         * */
        Map<String, String> appLogs = new HashMap<>();
        for (String appName : suspectedApps) {
//            List<DockerContainerLogResponse> dockerLogLines = dockerService.getContainerLogs(
//                    appName, project.getServerIP(), new DockerContainerLogRequest()
//            );

            //이렇게 하는 이유는 DockerContainerLogResponse에 형식 맞추고 해당 내용을 String으로 바꿔주다보니 생김(차라리 Response안에 String으로 변환하는 메서드가 있다면 메인 로직이 깔끔할것 같음)
//            String fullLogText = dockerLogLines.stream()
//                    .map(logLine -> {
//                        if (logLine.timestamp() != null) {
//                            return logLine.timestamp() + " " + logLine.message();
//                        } else {
//                            return logLine.message();
//                        }
//                    })
//                    .collect(Collectors.joining("\n"));
//
//            appLogs.put(appName, fullLogText);
        }


        // 2. AI FastAPI를 이용한 수정 파일 정보 받기 및 해당 파일 내용 수정
        List<PatchedFile> patchedFiles = new ArrayList<>();
        List<ResolveErrorResponse> resolveErrorResponses = new ArrayList<>();

        for (String appName : suspectedApps) {
            String appLog = appLogs.get(appName);
            List<GitlabTree> tree = appTrees.get(appName);

            Map<String, Object> diffRawPayload = new HashMap<>();
            diffRawPayload.put("commit", Map.of(
                    "title", "auto-generated commit",
                    "message", "generated by simulateSelfHealing()"
            ));
            diffRawPayload.put("diffs", gitlabCompareResponse.getDiffs());

            String diffJson;
            String treeJson;
            try {
                diffJson = objectMapper.writeValueAsString(diffRawPayload);
                treeJson = objectMapper.writeValueAsString(tree);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.AI_INFER_REQUEST_FAILED);
            }

            // 2-1. 의심되는 어플리케이션 'files' 찾기
            SuspectFileRequest suspectRequest = SuspectFileRequest.builder()
                    .diffRaw(diffJson)
                    .tree(treeJson)
                    .log(appLog)
                    .build();

            SuspectFileInnerResponse suspectFileInnerResponse = fastAIClient.requestSuspectFiles(suspectRequest).getResponse();
//            log.debug(">>>>>>>>>>>>>의심파일 찾기"+suspectDto.getResponse().getSuspectFiles().toString());

//            String summary = suspectFileResponse.getResponse().getErrorSummary();
//            String cause = suspectFileResponse.getResponse().getCause();
//            String hint = suspectFileResponse.getResponse().getResolutionHint();

            // 2-2. 의심되는 'file 원본 코드' 가져오기
            List<Map<String, String>> filesRaw = new ArrayList<>();
            for (var f : suspectFileInnerResponse.getSuspectFiles()) {
                String path = f.getPath();
                String code = gitlabService.getRawFileContent(accessToken, projectId, path, "master");
                filesRaw.add(Map.of("path", path, "code", code));
//                log.debug(">>>>>>>>>>>>>깃렙 api 파일 path: "+path+", 소스코드: "+code);
            }

            // 2-3. 의심되는 'file 해결 요약본' 가져오기
            String rawJson;
            try {
                rawJson = objectMapper.writeValueAsString(filesRaw);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.AI_RESOLVE_REQUEST_FAILED);
            }

            ResolveErrorResponse resolveDto = fastAIClient.requestResolveError(suspectFileInnerResponse, rawJson);
//            log.debug(">>>>>>>>>>>>>해결책 요약: "+resolveDto.toString());

            // 2-4. 해결 요약본을 토대로 '수정된 파일 코드' 받기
            for (var fix : resolveDto.getResponse().getFileFixes()) {
                String path = fix.getPath();
                String instruction = fix.getInstruction();
                String code = filesRaw.stream()
                        .filter(f -> f.get("path").equals(path))
                        .findFirst()
                        .map(f -> f.get("code"))
                        .orElse("");

                PatchFileRequest patchFileRequest = PatchFileRequest.builder()
                        .path(path)
                        .originalCode(code)
                        .instruction(instruction)
                        .build();

                PatchedFile patch = fastAIClient.requestPatchFile(patchFileRequest);
//                log.debug(">>>>>>>>>>>>>변경된 파일 내용: "+patch.getPatchedCode().toString());
                patchedFiles.add(patch);
            }
            resolveErrorResponses.add(resolveDto);
        }

        //3. Gitlab에 새 브랜치 생성과 수정된 파일들 커밋 진행 + Jenkins 빌드
        /**
         * 3-1. GitLab에 새로운 브랜치 생성
         * 문제점1: MONO면 master 고정, multi면 어떤걸 기준으로 나눠야할지?
         * project.getBackendoriginalProjectBranch();
         * project.getFrontendoriginalProjectBranch();
         *
         * 내용2: 새로운 branch에 대해서 webhook에 추가되야하므로 "ai/fix/*"내용 프로젝트 1번기능때 넣어줘야함!
         * 담당자: 이재훈
         * */
        String newProejctBranch = "ai/fix/" + System.currentTimeMillis();
        gitlabService.createBranch(accessToken, project.getId(), newProejctBranch, originalProjectBranch);


        // 3-2. GitLab에 AI를 통해 수정된 파일들 커밋
        if (!patchedFiles.isEmpty()) {
            String commitMessage = "Fix: AI auto fix by SEED";

            gitlabService.commitPatchedFiles(
                    accessToken,
                    project.getId(),
                    newProejctBranch,
                    commitMessage,
                    patchedFiles
            );
        }

         // 3-3. Jenkins 재빌드 트리거
        jenkinsService.triggerBuild(project.getId(), newProejctBranch);

        // 4. 빌드 결과 및 성공 여부에 따라 MR 생성, 반환값 필요
        // 4-1. Jenkins 빌드 결과 수집
        String status = jenkinsService.getLastBuild(projectId, accessToken).getStatus();
        boolean buildSucceeded = "SUCCESS".equalsIgnoreCase(status);

        // 4-2. AI 요약 보고서 가져오기
        Map<String, ReportResponse> reportResponses = new HashMap<>();

        for (int i = 0; i < resolveErrorResponses.size(); i++) {
            ResolveErrorResponse resolveDto = resolveErrorResponses.get(i);

            AIReportRequest reportRequest = AIReportRequest.builder()
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

            ReportResponse reportResponse = fastAIClient.requestErrorReport(reportRequest);

            // 앱 이름 정보가 resolveDto에 없으므로, 인덱스 기준으로 suspectedApps에서 가져옴
            String appName = suspectedApps.get(i);
            reportResponses.put(appName, reportResponse);
        }

        // 4-3. GitLab Merge Request 생성 (빌드 성공 시)
        if (buildSucceeded) {
            gitlabService.createMergeRequest(
                    accessToken,
                    project.getId(),
                    newProejctBranch,
                    originalProjectBranch,
                    "[AI 수정 제안] 빌드 자동 복구",
                    "AI가 수정한 코드를 기반으로 빌드가 성공했습니다. 검토 후 병합해주세요."
            );
        }

        // 4-4. 반환값 고민
    }
}

