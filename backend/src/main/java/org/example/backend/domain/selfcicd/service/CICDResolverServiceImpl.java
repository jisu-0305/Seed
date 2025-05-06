package org.example.backend.domain.selfcicd.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.log.DockerLogRequest;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
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
import org.example.backend.util.log.LogUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CICDResolverServiceImpl implements CICDResolverService {
    private final JenkinsService jenkinsService;
//    private final GitService gitService;
    private final DockerService dockerService;
//    private final FastAIAgent fastAIAgent;
    private final GitlabService gitlabService;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;

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
         * jenkins 로그를 조회하는 메서드 리팩토링 필요
         * 파라미터: 프로젝트 ip 또는 DNS, buildNumber
         * 진행 로직: 해당 DNS 또는 apiClient를 이용해서 buildNumber의 log가져오기(최대한 에러부분만 도려냈으면 좋겠음. 길게말고)
         * 담당자: 강승엽
         */
//        String jenkinsErrorLog = jenkinsService.getBuildLog(project.getServerIP(), buildNumber);

        // 1-2. 해당 프로젝트의 어플리케이션 목록 조회
        List<Application> apps = applicationRepository.findAllByProjectId(project.getId());
        List<String> appNames = apps.stream()
                .map(Application::getImageName)
                .toList();

        // 1-3. Git diff 정보 가져오기
        GitlabCompareResponse gitlabCompareResponse = gitlabService.getLatestMergeRequestDiff(accessToken, project.getId());

        // 1-4. AI API 호출: 1~3의 재료주고 의심되는 애플리케이션 추론 요청
        SuspectedFileRequest suspectRequest = SuspectedFileRequest.builder()
                .gitDiff(gitlabCompareResponse.getDiffs())
                .commitLog(gitlabCompareResponse.getCommit())
                .jenkinsLog(jenkinsErrorLog)
                .applicationNames(appNames)
                .build();

        List<String> suspectedApplications = fastAIAgent.getSuspectedApplications(suspectRequest);

        /**
         * 2-1. 해당 어플리케이션들의 트리 구조 가져오기
         * 문제점1: 현재 Project에는 backendDirectoryName, frontendDirectoryName 2가지 존재 결국 Nginx를 수정해야한다면? 관련된 코드는 어디서.
         *         추가로 log가져올때도 docker log 사용해야할거같은데 nginx는 별도의 log찾아야하는거 아닌가 싶음(결국 서버 접속 필요...? -> keypem저장?)
         * 담당자: 엔티티 - 강승엽, nginx 소스코드 저장 - 이재훈
         *
         * 문제점2: 폴더 트리 구조 다 가져오면 너무 내용이 많음, 서버면 트리구조의 depth 제한 가능 하지만 api는 불가능
         * 담당자: 박유진, 김지수, (Ai엮이면 공예슬 추가)
         * */
        for (String appFolderName : suspectedApplications) {
            String appPath = appFolderName+"/";
            List<GitlabTree> appTree = gitlabService.getTree(accessToken, project.getId(), appPath, true);
            // 필요한 경우 → 트리 구조로 가공
        }

        /**
         * 2-2. 해당 어플리케이션들의 log가져오기
         * 문제점: docker log 관련된 요청 해야하지않을까? 근데 nginx라면? + 현재 위에 내가 만든 서버에 직접 요청 방식 or dockerService의 요청 방식 2개 중 고민 필요
         * 담당자: 박유진, 김지수
         * */
//        Map<String, String> appLogs = new HashMap<>();
//        for (String appName : suspectedApplications) {
//            String logs = dockerService.getRecentLog(project, appName);
//            appLogs.put(appName, logs);
//        }

        // 2-3. AI API 호출: 문제 있는 파일 path 추론 요청
        List<String> filePaths = fastAIAgent.requestSuspectFiles(
                gitlabCompareResponse.getDiffs(),
                appTreeMap,
                jenkinsErrorLog
        );

        // 3-1. AI API 호출: 문제 있는 파일들 소스코드, ?, ? 제공 -> 수정된 코드들 받기
        List<PatchedFile> patches = new ArrayList<>();
        for (String path : filePaths) {
            String originalCode = gitlabService.getFile(accessToken, projectId, path, ref);
            String instruction = fastAIAgent.requestFixInstruction(jenkinsErrorLog, path, originalCode);
            PatchedFile patch = fastAIAgent.requestPatchFile(path, originalCode, instruction);
            patches.add(patch);
        }

        // 4-1. GitLab에 새로운 브랜치 생성
        String targetBranch = "master";
        String newBranch = "ai-fix-" + System.currentTimeMillis(); // ex) ai-fix-1715082540000
        gitlabService.createBranch(accessToken, project.getId(), newBranch, targetBranch);

        // 4-2. GitLab에 수정된 파일들 커밋
        for (PatchedFile patch : patchedFiles) {
            gitlabService.commitFileUpdate(
                    accessToken,
                    project.getId(),
                    newBranch,
                    patch.getPath(),
                    patch.getPatchedCode(),
                    "AI 자동 수정: " + patch.getPath()
            );
        }

        // 4-3. Jenkins 재빌드 트리거
        jenkinsService.triggerBuild(project.getServerIP(), newBranch);

        // 5. Jenkins 빌드 결과 수집 및 AI 요약 생성
        boolean buildSucceeded = jenkinsService.checkLastBuildSuccess(project.getServerIP(), newBranch); // 구현 필요

        String summary = buildSucceeded
                ? "AI가 수정한 코드를 기반으로 정상 작동합니다."
                : "빌드 실패: AI 수정 코드 반영 후에도 문제가 발생했습니다.";

        fastAIAgent.generateSummaryReport(
                fixInstructions,
                summary,
                buildSucceeded ? "AI가 정상적으로 문제를 해결했습니다." : "추가적인 디버깅이 필요합니다."
        );

        // 6. GitLab Merge Request 생성 (빌드 성공 시)
        if (buildSucceeded) {
            gitlabService.createMergeRequest(
                    accessToken,
                    project.getId(),
                    newBranch,
                    targetBranch,
                    "[AI 수정 제안] 빌드 자동 복구",
                    "AI가 수정한 코드를 기반으로 빌드가 성공했습니다. 검토 후 병합해주세요."
            );
        }
    }
}

