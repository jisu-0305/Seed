package org.example.backend.domain.selfcicd.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.log.DockerLogRequest;
import org.example.backend.controller.response.log.DockerLogResponse;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.domain.jenkins.service.JenkinsService;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.util.log.LogUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SelfCICDServiceImpl implements SelfCICDService {
    private final JenkinsService jenkinsService;
//    private final GitService gitService;
    private final DockerService dockerService;
//    private final FastAIAgent fastAIAgent;
    private final GitlabService gitlabService;
    private final ProjectRepository projectRepository;

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

    // 🔧 메인 로직
    @Override
    public void handleSelfHealingCI(Long projectId, int buildNumber, String accessToken) {
        // 1. 실패한 Jenkins 로그 조회
        String errorLog = jenkinsService.getBuildLog(buildNumber);

        // 2. 해당 프로젝트에 연결된 서버, 컨테이너 정보 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 없음"));

//        List<String> dockerLogs = dockerService.getLogsForAllApplications(project);
//
//        // 3. git diff + tree + commit info 수집
//        GitDiffData gitDiff = gitService.getDiff(project, accessToken);
//        GitTreeData tree = gitService.getTree(project, accessToken);
//        String commitLog = gitService.getCommitLog(project, accessToken);
//
//        // 4. suspect file 요청 (에러로그 + git diff + tree)
//        List<SuspectFile> suspectFiles = fastAIAgent.requestFilePaths(errorLog, gitDiff, tree);
//
//        // 5. AI에 각 파일별 수정 지시 요청
//        List<FileFixInstruction> fixInstructions = fastAIAgent.resolveFixInstructions(
//                errorLog, gitDiff, suspectFiles
//        );
//
//        // 6. 파일 전체 코드 불러온 뒤 AI에 최종 패치 요청
//        List<PatchedFile> patchedFiles = new ArrayList<>();
//        for (FileFixInstruction fix : fixInstructions) {
//            String originalCode = gitService.getFileContent(project, fix.getPath(), accessToken);
//            PatchedFile patch = fastAIAgent.requestPatchFile(fix.getPath(), originalCode, fix.getInstruction());
//            patchedFiles.add(patch);
//        }
//
//        // 7. GitLab 브랜치 생성 + 수정 커밋 + PR
//        gitlabService.createBranchCommitAndMR(project, patchedFiles, accessToken);

        // 8. Jenkins 재배포 트리거
        jenkinsService.triggerBuild();
    }
}

