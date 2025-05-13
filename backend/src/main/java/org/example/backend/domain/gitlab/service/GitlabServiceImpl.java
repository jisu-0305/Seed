package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.response.gitlab.CommitResponse;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitlabServiceImpl implements GitlabService {

    private final GitlabApiClient gitlabApiClient;

    /* Push _ webhook 생성 */
    @Override
    public void createPushWebhook(String gitlabPersonalAccessToken, Long projectId, String hookUrl, String branchFilter) {
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);
        gitlabApiClient.registerPushWebhook(validGitlabAccessToken, projectId, hookUrl, branchFilter);
    }

    /* Push 트리거 */
    @Override
    public String triggerPushEvent(String gitlabPersonalAccessToken, Long projectId, String branch) {

        String validToken = tokenValidCheck(gitlabPersonalAccessToken);
        branchValidCheck(validToken, projectId, branch);

        String filePath = String.format("trigger-%d-%s.txt", Instant.now().toEpochMilli(), UUID.randomUUID());

        CommitAction create = CommitAction.builder()
                .action("create")
                .filePath(filePath)
                .content("triggered at " + Instant.now())
                .build();

        CommitAction delete = CommitAction.builder()
                .action("delete")
                .filePath(filePath)
                .build();

        List<CommitAction> actions = List.of(create, delete);

        CommitResponse resp = gitlabApiClient.submitCommit(
                validToken,
                projectId,
                branch,
                "chore: trigger Jenkins build by SEED",
                actions
        );

        log.debug("푸시 트리거 완료: project={}, branch={}, webUrl={}", projectId, branch, resp.getWebUrl());

        return resp.getWebUrl();
    }

    /* MR생성 */
    @Override
    public MergeRequestCreateResponse createMergeRequest(String gitlabPersonalAccessToken,
                                                         Long projectId,
                                                         String sourceBranch,
                                                         String targetBranch,
                                                         String title,
                                                         String description
    ) {

        // 유효성 검사
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);
        branchValidCheck(validGitlabAccessToken, projectId, sourceBranch);
        branchValidCheck(validGitlabAccessToken, projectId, targetBranch);

        return gitlabApiClient.submitMergeRequest(
                validGitlabAccessToken,
                projectId,
                sourceBranch,
                targetBranch,
                title,
                description
        );

    }

    /* 브랜치 생성*/
    @Override
    public GitlabBranch createBranch(String gitlabPersonalAccessToken, Long projectId, String branch, String ref) {
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);
        return gitlabApiClient.submitBranchCreation(validGitlabAccessToken, projectId, branch, ref);
    }

    /*브랜치 삭제*/
    @Override
    public void deleteBranch(String gitlabPersonalAccessToken, Long projectId, String branch) {
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);
        gitlabApiClient.submitBranchDeletion(validGitlabAccessToken, projectId, branch);
    }

    /*레포지토리 목록 조회*/
    @Override
    public List<GitlabProject> getProjects(String gitlabPersonalAccessToken) {
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);
        log.info(">>>>>>>>>>>>>>>>> {}",validGitlabAccessToken);
        int page = 1;
        int perPage = 100;

        return gitlabApiClient.requestProjectList(validGitlabAccessToken, page, perPage);
    }

    /* 레포지토리 단건 조회 (URL) */
    @Override
    public GitlabProject getProjectByUrl(String gitlabPersonalAccessToken, String repoUrl) {
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);

        String repoPath = repoUrl.startsWith("http")
                ? java.net.URI.create(repoUrl).getPath().substring(1)
                : repoUrl;

        return gitlabApiClient.requestProjectInfo(validGitlabAccessToken, repoPath);
    }

    /* Diff 1 ) 최신 MR 기준 diff 조회 */
    @Override
    public Mono<GitlabCompareResponse> fetchLatestMrDiff(String gitlabPersonalAccessToken, Long projectId) {
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);
        int page = 1;
        int perPage = 1;

        return gitlabApiClient.requestMergedMrs(validGitlabAccessToken, projectId, page, perPage)
                .flatMap(mrs -> {
                    if (mrs.isEmpty()) {
                        return Mono.error(new BusinessException(ErrorCode.GITLAB_NO_MERGE_REQUESTS));
                    }
                    GitlabMergeRequest latest = mrs.get(0);

                    return gitlabApiClient.requestMrDetail(validGitlabAccessToken, projectId, latest.getIid())
                            .flatMap(detail -> {
                                String base = detail.getDiffRefs().getBaseSha();
                                String head = detail.getDiffRefs().getHeadSha();
                                return gitlabApiClient.requestCommitComparison(validGitlabAccessToken, projectId, base, head);
                            });
                });
    }

    /* Diff 2 ) 커밋 간 변경사항 조회 (from-to) */
    @Override
    public Mono<GitlabCompareResponse> compareCommits(String gitlabPersonalAccessToken, Long projectId, String from, String to) {
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);
        return gitlabApiClient.requestCommitComparison(validGitlabAccessToken, projectId, from, to);
    }

    /* 레포지토리 tree 구조 조회  */
    @Override
    public List<GitlabTree> getRepositoryTree(String gitlabPersonalAccessToken, Long projectId, String path, boolean recursive, String branchName) {
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);
        int page = 1;
        int perPage = 100;

        return gitlabApiClient.requestRepositoryTree(
                validGitlabAccessToken,
                projectId,
                path,
                recursive,
                page,
                perPage,
                branchName
        );
    }

    /* 파일 원본 조회  */
    @Override
    public String getRawFileContent(String gitlabPersonalAccessToken, Long projectId, String path, String ref) {
        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);
        return gitlabApiClient.requestRawFileContent(validGitlabAccessToken, projectId, path, ref);
    }

    /* 파일 수정 후 커밋 */
    @Override
    public void commitPatchedFiles(String gitlabPersonalAccessToken, Long projectId, String branch, String commitMessage, List<PatchedFile> patchedFiles) {

        String validGitlabAccessToken = tokenValidCheck(gitlabPersonalAccessToken);

        List<CommitAction> actions = patchedFiles.stream()
                .map(patchedFile -> CommitAction.builder()
                        .action("update")
                        .filePath(patchedFile.getPath())
                        .content(patchedFile.getPatchedCode())
                        .build()
                )
                .collect(Collectors.toList());

        gitlabApiClient.submitCommit(validGitlabAccessToken, projectId, branch, commitMessage, actions);

    }

    /* TODO: 공통 로직 _ pat 등록여부 체크 로직 작성 필요 */
    private String tokenValidCheck(String gitlabPersonalAccessToken) {

        // 일단 접두사로 glpat있는지 정도만 체크함.(있으면 빼야하기 때문.)
        if (gitlabPersonalAccessToken != null && gitlabPersonalAccessToken.startsWith("glpat-")) {
            return gitlabPersonalAccessToken.substring("glpat-".length());
        }

        return gitlabPersonalAccessToken;

    }

    private void branchValidCheck(String gitlabPersonalAccessToken, Long projectId, String branch) {
        gitlabApiClient.validateBranchExists(gitlabPersonalAccessToken, projectId, branch);
    }

}
