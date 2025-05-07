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
import reactor.core.publisher.Mono;

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

    /* Push _ webhook 생성 */
    @Override
    public void createPushWebhook(String accessToken, Long projectId, String hookUrl, String branchFilter) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        gitlabApiClient.registerPushWebhook(gitlabAccessToken, projectId, hookUrl, branchFilter);
    }

    /* Push 트리거 */
    @Override
    public void triggerPushEvent(String accessToken, Long projectId, String branch) {

        // 유효성 검사
        String gitlabAccessToken = tokenValidCheck(accessToken);
        branchValidCheck(gitlabAccessToken, projectId, branch);

        // 유니크한 파일명
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

        gitlabApiClient.submitCommit(gitlabAccessToken, projectId, branch, commitMessage, actions);

        log.debug(">>>>>>>>> 푸시 트리거 동작 완료 for projectId={}, branch={}, filePath={}", projectId, branch, filePath);

    }

    /* MR생성 */
    @Override
    public MergeRequestCreateResponse createMergeRequest(String accessToken,
                                                         Long projectId,
                                                         String sourceBranch,
                                                         String targetBranch,
                                                         String title,
                                                         String description
    ) {

        // 유효성 검사
        String gitlabAccessToken = tokenValidCheck(accessToken);
        branchValidCheck(gitlabAccessToken, projectId, sourceBranch);
        branchValidCheck(gitlabAccessToken, projectId, targetBranch);

        return gitlabApiClient.submitMergeRequest(
                gitlabAccessToken,
                projectId,
                sourceBranch,
                targetBranch,
                title,
                description
        );

    }

    /* 브랜치 생성*/
    @Override
    public GitlabBranch createBranch(String accessToken, Long projectId, String branch, String ref) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        return gitlabApiClient.submitBranchCreation(gitlabAccessToken, projectId, branch, ref);
    }

    /*브랜치 삭제*/
    @Override
    public void deleteBranch(String accessToken, Long projectId, String branch) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        gitlabApiClient.submitBranchDeletion(gitlabAccessToken, projectId, branch);
    }

    /*레포지토리 목록 조회*/
    @Override
    public List<GitlabProject> getProjects(String accessToken) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        int page = 1;
        int perPage = 100;

        return gitlabApiClient.requestProjectList(gitlabAccessToken, page, perPage);
    }

    /* 레포지토리 단건 조회 (URL) */
    @Override
    public GitlabProject getProjectByUrl(String accessToken, String repoUrl) {
        String gitlabAccessToken = tokenValidCheck(accessToken);

        String repoPath = repoUrl.startsWith("http")
                ? java.net.URI.create(repoUrl).getPath().substring(1)
                : repoUrl;

        return gitlabApiClient.requestProjectInfo(gitlabAccessToken, repoPath);
    }

    /* Diff 1 ) 최신 MR 기준 diff 조회 */
    @Override
    public Mono<GitlabCompareResponse> fetchLatestMrDiff(String accessToken, Long projectId) {
        String token = tokenValidCheck(accessToken);
        int page = 1;
        int perPage = 1;

        return gitlabApiClient.requestMergedMrs(token, projectId, page, perPage)
                .flatMap(mrs -> {
                    if (mrs.isEmpty()) {
                        return Mono.error(new BusinessException(ErrorCode.GITLAB_NO_MERGE_REQUESTS));
                    }
                    GitlabMergeRequest latest = mrs.get(0);

                    return gitlabApiClient.requestMrDetail(token, projectId, latest.getIid())
                            .flatMap(detail -> {
                                String base = detail.getDiffRefs().getBaseSha();
                                String head = detail.getDiffRefs().getHeadSha();
                                return gitlabApiClient.requestCommitComparison(token, projectId, base, head);
                            });
                });
    }

    /* Diff 2 ) 커밋 간 변경사항 조회 (from-to) */
    @Override
    public Mono<GitlabCompareResponse> compareCommits(String accessToken, Long projectId, String from, String to) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        return gitlabApiClient.requestCommitComparison(gitlabAccessToken, projectId, from, to);
    }

    /* 레포지토리 tree 구조 조회  */
    @Override
    public List<GitlabTree> getRepositoryTree(String accessToken, Long projectId, String path, boolean recursive) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        int page = 1;
        int perPage = 100;

        return gitlabApiClient.requestRepositoryTree(gitlabAccessToken, projectId, path, recursive, page, perPage);
    }

    /* 파일 원본 조회  */
    @Override
    public String getRawFileContent(String accessToken, Long projectId, String path, String ref) {
        String gitlabAccessToken = tokenValidCheck(accessToken);
        return gitlabApiClient.requestRawFileContent(gitlabAccessToken, projectId, path, ref);
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

    private void branchValidCheck(String gitlabAccessToken, Long projectId, String branch) {
        gitlabApiClient.validateBranchExists(gitlabAccessToken, projectId, branch);
    }

}
