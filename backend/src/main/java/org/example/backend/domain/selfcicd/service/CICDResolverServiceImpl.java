package org.example.backend.domain.selfcicd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.log.DockerLogRequest;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.jenkins.JenkinsBuildListResponse;
import org.example.backend.controller.response.log.DockerLogResponse;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.domain.jenkins.service.JenkinsService;
import org.example.backend.domain.project.entity.Application;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.repository.ApplicationRepository;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.util.fastai.FastAIClient;
import org.example.backend.util.fastai.dto.suspectapp.InferAppRequest;
import org.example.backend.util.log.LogUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CICDResolverServiceImpl implements CICDResolverService {
    private final JenkinsService jenkinsService;
    private final DockerService dockerService;
    private final FastAIClient fastAIClient;
    private final GitlabService gitlabService;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        /**
         * 1-1. 실패한 Jenkins 로그 조회
         * 내용: jenkins 로그를 조회하는 메서드 리팩토링 필요
         * 파라미터: 프로젝트 ip 또는 DNS, buildNumber
         * 진행 로직: 해당 DNS 또는 apiClient를 이용해서 buildNumber의 log가져오기(최대한 에러부분만 도려냈으면 좋겠음. 길게말고)
         * 담당자: 강승엽
         */
        JenkinsBuildListResponse lastBuild = jenkinsService.getLastBuild(projectId, accessToken);
        int buildNumber = lastBuild.getBuildNumber();
        String jenkinsErrorLog = jenkinsService.getBuildLog(buildNumber, projectId, accessToken);

        // 1-2. 해당 프로젝트의 어플리케이션 목록 조회
        List<Application> apps = applicationRepository.findAllByProjectId(project.getId());
        List<String> appNames = apps.stream()
                .map(Application::getImageName)
                .toList();

        // 1-3. Git diff 정보 가져오기 -> 관련해서 GitlabCompareCommit.id 필드가 실제 커밋 id인지 사실 여부 확인 필요 - 담당자: 박유진
        GitlabCompareResponse gitlabCompareResponse = gitlabService.fetchLatestMrDiff(accessToken, project.getId()).block();
        String ref = "";
        if (gitlabCompareResponse != null) {
            ref = gitlabCompareResponse.getCommit().getId();
        }

        //1-4. AI API 호출: 1~3의 재료주고 의심되는 애플리케이션 추론 요청
        InferAppRequest inferAppRequest = InferAppRequest.builder()
                .gitDiff(gitlabCompareResponse.getDiffs())
                .jenkinsLog(jenkinsErrorLog)
                .applicationNames(appNames)
                .build();

        List<String> suspectedApps= fastAIClient.requestInferApplications(inferAppRequest);

        //2-1. 해당 어플리케이션들의 트리 구조 가져오기
        Map<String, String> appToFolderMap = Map.of(
                "spring", "backend",
                "react", "frontend"
        );

        Map<String, List<GitlabTree>> appTrees = new HashMap<>();

        for (String appName : suspectedApps) {
            String folder = appToFolderMap.getOrDefault(appName, appName);
            String appPath = folder + "/";

//            List<GitlabTree> appTree = gitlabService.getRepositoryTree(accessToken, project.getId(), appPath, true);
//            appTrees.put(appName, appTree);
        }

        /**
         * 2-2. 해당 어플리케이션들의 log가져오기
         * 문제점: docker log 관련된 요청 해야하지않을까? 근데 nginx라면? + 현재 위에 내가 만든 서버에 직접 요청 방식 or dockerService의 요청 방식 2개 중 고민 필요
         * 담당자: 박유진, 김지수
         *
         * 문제점2: 현재 Project에는 backendDirectoryName, frontendDirectoryName 2가지 존재 결국 Nginx를 수정해야한다면? 관련된 코드는 어디서.
         *         추가로 log가져올때도 docker log 사용해야할거같은데 nginx는 별도의 log찾아야하는거 아닌가 싶음(결국 서버 접속 필요...? -> keypem저장?)
         * 담당자: 엔티티 - 강승엽, nginx 소스코드 저장 - 이재훈
         * */
        Map<String, String> appLogs = new HashMap<>();
//        for (String appName : suspectedApps) {
//            String logs = dockerService.getRecentLog(project, appName); // 도커 기반 로그 수집
//            appLogs.put(appName, logs);
//        }

//        // 3. AI호출 로직 작성
//        List<PatchedFile> patchedFiles = new ArrayList<>();
//
//        for (String appName : suspectedApps) {
//            List<GitlabTree> tree = appTrees.get(appName);
//            String appLog = appLogs.get(appName);
//
//            // 3-1: 의심 파일 및 오류 요약 추론
//            JsonNode response;
//            try {
//                String diffJson = objectMapper.writeValueAsString(gitlabCompareResponse.getDiffs());
//                String aiRawResponse = fastAIClient.requestSuspectFiles(diffJson, tree.toString(), appLog);
//                response = objectMapper.readTree(aiRawResponse).get("response");
//            } catch (JsonProcessingException e) {
//                throw new BusinessException(ErrorCode.AI_JSON_PROCESSING_FAILED);
//            }
//
//            String errorSummary = response.get("errorSummary").asText();
//            String cause = response.get("cause").asText();
//            String resolutionHint = response.get("resolutionHint").asText();
//
//            // suspectFiles path/code 추출
//            List<Map<String, String>> filesRaw = new ArrayList<>();
//            for (JsonNode fileNode : response.get("suspectFiles")) {
//                String path = fileNode.get("path").asText();
//                String code = gitlabService.getRawFileContent(accessToken, projectId, path, ref);
//                filesRaw.add(Map.of("path", path, "code", code));
//            }
//
//            String filesRawJson;
//            try {
//                filesRawJson = objectMapper.writeValueAsString(filesRaw);
//            } catch (JsonProcessingException e) {
//                throw new BusinessException(ErrorCode.AI_JSON_PROCESSING_FAILED);
//            }
//            // 3-2: 수정 지시 요청
//            String resolveResponse = fastAIClient.requestResolveError(
//                    errorSummary, cause, resolutionHint, filesRawJson
//            );
//
//            // 3-3: 지시 기반 패치
//            JsonNode fileFixes;
//            try {
//                fileFixes = objectMapper.readTree(resolveResponse).get("fileFixes");
//            } catch (JsonProcessingException e) {
//                throw new BusinessException(ErrorCode.AI_JSON_PROCESSING_FAILED);
//            }
//
//            for (JsonNode fix : fileFixes) {
//                String path = fix.get("path").asText();
//                String instruction = fix.get("instruction").asText();
//
//                String originalCode = filesRaw.stream()
//                        .filter(f -> f.get("path").equals(path))
//                        .findFirst()
//                        .map(f -> f.get("code"))
//                        .orElse("");
//
//                PatchedFile patch = fastAIClient.requestPatchFile(path, originalCode, instruction);
//                patchedFiles.add(patch);
//            }
//        }

        /**
         * 4-1. GitLab에 새로운 브랜치 생성
         * 문제점1: MONO면 master 고정, multi면 어떤걸 기준으로 나눠야할지?
         * project.getBackendBranchName();
         * project.getFrontendBranchName();
         *
         * 내용2: 새로운 branch에 대해서 webhook에 추가되야하므로 "ai/fix/*"내용 프로젝트 1번기능때 넣어줘야함!
         * 담당자: 이재훈
         * */
//        String targetBranch ="";
//        if(project.getStructure().equals(MONO)) targetBranch = "master";
////        else BackendBranch ? FrontBranch
//
//        String newBranch = "ai/fix/" + System.currentTimeMillis();
//        gitlabService.createBranch(accessToken, project.getId(), newBranch, targetBranch);

        /**
         * 4-2. GitLab에 수정된 파일들 커밋
         * 내용: File 수정해서 커밋 남길수있는 gitlabService 메서드 필요, 이전에 관련된 API 들었던걸로 기억
         * 담당자: 박유진
         * */

//        if (!patchedFiles.isEmpty()) {
//            String commitMessage = "Fix: AI auto fix by SEED";
//
//            gitlabService.commitPatchedFiles(
//                    accessToken,
//                    project.getId(),
//                    newBranch,
//                    commitMessage,
//                    patchedFiles
//            );
//        }

        /**
         * 4-3. Jenkins 재빌드 트리거
         * 내용: 기존 gitlabService.triggerPushEvent가 불가능한 이유 -> 결국 여기서 발생한 pushevent를 jenkins쪽에서 계속 받도록 정규식 처리해야함, 그말인 즉슨 commit남길때 이미 자동배포 자꾸 돌아가게됨
         *      따라서 모든 내용 적용나고 한번 트리거 작동시켜야하므로 jenkins.triggerBuild가 맞음
         * 문제점: triggerBuild의 파라미터에 사용자 프로젝트의 jenkins ip or DNS를 줘야함 -> 추가 파라미터, 로직 필요
         * 담당자: 강승엽
         * */
//        jenkinsService.triggerBuild(project.getServerIP(), newBranch);
//        gitlabService.triggerPushEvent(accessToken, projectId, newBranch);  //고민했지만 삭제 필요

        /**
         * 5. Jenkins 빌드 결과 수집 및 AI 요약 생성
         * 내용: 마지막으로 jenkins의 해당 job에서 build된 내용 결과 가져오기
         * 담당자: 강승엽
         */
        String status = jenkinsService.getLastBuild(projectId, accessToken).getStatus();
        boolean buildSucceeded = "SUCCESS".equalsIgnoreCase(status);
//
//        String summary = buildSucceeded ? "AI가 수정한 코드를 기반으로 정상 작동합니다." : "빌드 실패: AI 수정 코드 반영 후에도 문제가 발생했습니다.";
//
//        String errorSummary = "AI 자동 수정 빌드 결과 요약";
//        String cause = buildSucceeded
//                ? "최근 커밋에서 발생한 문제는 AI가 수정한 코드에 의해 해결되었습니다."
//                : "수정 후에도 동일한 문제가 지속되어, 근본 원인이 아직 해결되지 않았을 가능성이 있습니다.";
//        String finalResolution = buildSucceeded
//                ? "AI가 수정한 코드를 기반으로 빌드에 성공했습니다."
//                : "AI 수정 후에도 빌드 실패가 발생하여 추가적인 디버깅이 필요합니다.";
//
//        AIReportRequest reportRequest = AIReportRequest.builder()
//                .fileFixes(fixInstructions.stream().map(fix -> new AIReportRequest.FileFix(
//                        fix.getPath(),
//                        fix.getInstruction(),
//                        fix.getExplanation()
//                )).toList())
//                .resolutionReport(new AIReportRequest.ResolutionReport(
//                        errorSummary,
//                        cause,
//                        finalResolution
//                ))
//                .build();
//
//        fastAIClient.generateSummaryReport(reportRequest);
//
//        // 6. GitLab Merge Request 생성 (빌드 성공 시)
//        if (buildSucceeded) {
//            gitlabService.createMergeRequest(
//                    accessToken,
//                    project.getId(),
//                    newBranch,
//                    targetBranch,
//                    "[AI 수정 제안] 빌드 자동 복구",
//                    "AI가 수정한 코드를 기반으로 빌드가 성공했습니다. 검토 후 병합해주세요."
//            );
//        }
    }
}

