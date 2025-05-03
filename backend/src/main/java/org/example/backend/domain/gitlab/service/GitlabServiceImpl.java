package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.request.gitlab.ProjectUrlRequest;
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
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.listProjects(user.getGitlabAccessToken());
    }

    @Override
    public GitlabProject getProjectInfo(String accessToken, ProjectUrlRequest request) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.getProjectInfo(user.getGitlabAccessToken(), request.getRepositoryUrl());
    }

    @Override
    public List<GitlabTree> getTree(String accessToken, Long projectId, String path, boolean recursive) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.listTree(user.getGitlabAccessToken(), projectId, path, recursive);
    }

    @Override
    public String getFile(String accessToken, Long projectId, String path, String ref) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.getRawFile(user.getGitlabAccessToken(), projectId, path, ref);
    }

    @Override
    public GitlabCompareResponse getDiff(String accessToken, Long projectId, String from, String to) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.compareCommits(user.getGitlabAccessToken(), projectId, from, to);
    }

    @Override
    public GitlabBranch createBranch(String accessToken, Long projectId, String branch, String ref) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.createBranch(user.getGitlabAccessToken(), projectId, branch, ref);
    }

    @Override
    public void createPushWebhook(String accessToken, Long projectId, String hookUrl, String branchFilter) {

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        gitlabApiClient.createProjectHook(user.getGitlabAccessToken(), projectId, hookUrl, branchFilter);

    }

    @Override
    public String deleteBranch(String accessToken, Long projectId, String branch) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        gitlabApiClient.deleteBranch(user.getGitlabAccessToken(), projectId, branch);

        return branch;
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

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        // 브랜치 여부 먼저 확인하기
        gitlabApiClient.getBranch(user.getGitlabAccessToken(), projectId, sourceBranch);
        gitlabApiClient.getBranch(user.getGitlabAccessToken(), projectId, targetBranch);

        return gitlabApiClient.createMergeRequest(
                user.getGitlabAccessToken(),
                projectId,
                sourceBranch,
                targetBranch,
                title,
                description
        );
    }

    @Override
    public GitlabCompareResponse getLatestMergeRequestDiff(String accessToken, Long projectId) {

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        // 1) 최신 mr 하나 조회
        List<GitlabMergeRequest> mrs =
                gitlabApiClient.listMergeRequests(user.getGitlabAccessToken(), projectId, 1, 1);
        if (mrs == null || mrs.isEmpty()) {
            throw new BusinessException(ErrorCode.GITLAB_NO_MERGE_REQUESTS);
        }
        GitlabMergeRequest latest = mrs.get(0);

        // 2)mr 상세 조회해서 diff_refs 가져오기
        GitlabMergeRequest detail =
                gitlabApiClient.getMergeRequest(user.getGitlabAccessToken(), projectId, latest.getIid());
        String base = detail.getDiffRefs().getBaseSha();
        String head = detail.getDiffRefs().getHeadSha();

        // 3) 비교
        return gitlabApiClient.compareCommits(user.getGitlabAccessToken(), projectId, base, head);
    }

    @Override
    public void triggerPushEvent(String accessToken, Long projectId, String branch) {

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getGitlabAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        // 브랜치 존재 확인 (없으면 404)
        gitlabApiClient.getBranch(user.getGitlabAccessToken(), projectId, branch);

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
        log.debug(">>>>>>>>>> 커밋 action : {}", actions);

        // 7) 두 액션을 묶어서 한 번의 커밋으로 푸시 이벤트 트리거
        gitlabApiClient.createCommit(
                user.getGitlabAccessToken(),
                projectId,
                branch,
                "chore: trigger Jenkins build by SEED",
                actions
        );
        log.debug(">>>>>>>>> 푸시 트리거 동작 완료 for projectId={}, branch={}, filePath={}",
                projectId, branch, filePath);
    }

}
