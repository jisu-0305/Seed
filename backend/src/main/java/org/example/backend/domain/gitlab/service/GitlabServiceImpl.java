package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitlabServiceImpl implements GitlabService {

    private final UserRepository userRepository;
    private final GitlabApiClient gitlabApiClient;
    private final RedisSessionManager redisSessionManager;

    @Override
    public List<GitlabProject> getProjects(String accessToken) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        return gitlabApiClient.listProjects(gitlabAccessToken);
    }

    @Override
    public GitlabProject getProjectInfo(String accessToken, String repoUrl) {
        String gitlabAccessToken = tokenValidCheck(accessToken);

        String repoPath = repoUrl.startsWith("http")
                ? java.net.URI.create(repoUrl).getPath().substring(1)
                : repoUrl;

        return gitlabApiClient.getProjectInfo(gitlabAccessToken, repoPath);
    }

    @Override
    public List<GitlabTree> getTree(String accessToken, Long projectId, String path, boolean recursive) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        return gitlabApiClient.listTree(gitlabAccessToken, projectId, path, recursive);
    }

    @Override
    public String getFile(String accessToken, Long projectId, String path, String ref) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        return gitlabApiClient.getRawFile(gitlabAccessToken, projectId, path, ref);
    }

    @Override
    public GitlabCompareResponse getDiff(String accessToken, Long projectId, String from, String to) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        return gitlabApiClient.compareCommits(gitlabAccessToken, projectId, from, to);
    }

    @Override
    public GitlabBranch createBranch(String accessToken, Long projectId, String branch, String ref) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        return gitlabApiClient.createBranch(gitlabAccessToken, projectId, branch, ref);
    }

    @Override
    public void createPushWebhook(String accessToken, Long projectId, String hookUrl, String branchFilter) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        gitlabApiClient.createProjectHook(gitlabAccessToken, projectId, hookUrl, branchFilter);
    }

    @Override
    public void deleteBranch(String accessToken, Long projectId, String branch) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        gitlabApiClient.deleteBranch(gitlabAccessToken, projectId, branch);
    }

    @Override
    public MergeRequestCreateResponse createMergeRequest(
            String accessToken,
            Long projectId,
            String sourceBranch,
            String targetBranch,
            String title,
            String description
    ) {
        String gitlabAccessToken = tokenValidCheck(accessToken);

        // 브랜치 여부 먼저 확인하기
        gitlabApiClient.getBranch(gitlabAccessToken, projectId, sourceBranch);
        gitlabApiClient.getBranch(gitlabAccessToken, projectId, targetBranch);

        return gitlabApiClient.createMergeRequest(
                gitlabAccessToken,
                projectId,
                sourceBranch,
                targetBranch,
                title,
                description
        );
    }

    @Override
    public GitlabCompareResponse getLatestMergeRequestDiff(String accessToken, Long projectId) {

        String gitlabAccessToken = tokenValidCheck(accessToken);

        // 최신 mr 하나 조회
        List<GitlabMergeRequest> mrs = gitlabApiClient.listMergeRequests(gitlabAccessToken, projectId, 1, 1);
        if (mrs == null || mrs.isEmpty()) {
            throw new BusinessException(ErrorCode.GITLAB_NO_MERGE_REQUESTS);
        }
        GitlabMergeRequest latest = mrs.get(0);

        // mr 상세 조회해서 diff_refs 가져오기
        GitlabMergeRequest detail = gitlabApiClient.getMergeRequest(gitlabAccessToken, projectId, latest.getIid());
        String base = detail.getDiffRefs().getBaseSha();
        String head = detail.getDiffRefs().getHeadSha();

        // 비교
        return gitlabApiClient.compareCommits(gitlabAccessToken, projectId, base, head);

    }

    @Override
    public void triggerPushEvent(String accessToken, Long projectId, String branch) {

        String gitlabAccessToken = tokenValidCheck(accessToken);

        // 브랜치 존재 확인
        gitlabApiClient.getBranch(gitlabAccessToken, projectId, branch);

        // 유니크한 파일명 생성 (루트에 바로)
        String filePath = String.format(
                "trigger-%d-%s.txt",
                Instant.now().toEpochMilli(),
                UUID.randomUUID()
        );

        // 더미 파일 생성
        CommitAction create = new CommitAction();
        create.setAction("create");
        create.setFile_path(filePath);
        create.setContent("triggered at " + Instant.now());

        // 바로 삭제
        CommitAction delete = new CommitAction();
        delete.setAction("delete");
        delete.setFile_path(filePath);

        List<CommitAction> actions = List.of(create, delete);
        String commitMessage = "chore: trigger Jenkins build by SEED";
        
        gitlabApiClient.createCommit(gitlabAccessToken, projectId, branch, commitMessage, actions);

        log.debug(">>>>>>>>> 푸시 트리거 동작 완료 for projectId={}, branch={}, filePath={}", projectId, branch, filePath);

    }


    /* 공통 로직 */
    private String tokenValidCheck(String accessToken) {

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
           throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return user.getGitlabAccessToken();

    }

}
