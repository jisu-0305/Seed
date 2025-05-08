package org.example.backend.domain.gitlab.service;

import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GitlabApiClient {

    void registerPushWebhook(String gitlabAccessToken, Long projectId, String hookUrl, String pushEventsBranchFilter);

    void submitCommit(String gitlabAccessToken, Long projectId, String branch, String commitMessage, List<CommitAction> action);

    MergeRequestCreateResponse submitMergeRequest(String accessToken,
                                                  Long projectId,
                                                  String sourceBranch,
                                                  String targetBranch,
                                                  String title,
                                                  String description);

    GitlabBranch submitBranchCreation(String accessToken, Long projectId, String branch, String ref);

    void submitBranchDeletion(String accessToken, Long projectId, String branch);

    List<GitlabProject> requestProjectList(String gitlabAccessToken, int page, int perPage);

    GitlabProject requestProjectInfo(String gitlabAccessToken, String projectUrl);

    Mono<List<GitlabMergeRequest>> requestMergedMrs(String accessToken, Long projectId, int page, int perPage);

    Mono<GitlabMergeRequest> requestMrDetail(String accessToken, Long projectId, Long mergeRequestIid);

    Mono<GitlabCompareResponse> requestCommitComparison(String accessToken, Long projectId, String from, String to);


    List<GitlabTree> requestRepositoryTree(
            String gitlabAccessToken,
            Long projectId,
            String path,
            boolean recursive,
            int page,
            int perPage,
            String branchName
    );

    String requestRawFileContent(String gitlabAccessToken, Long projectId, String path, String ref);

    void validateBranchExists(String accessToken, Long projectId, String branchName);

}
