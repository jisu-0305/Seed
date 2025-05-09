package org.example.backend.domain.gitlab.service;

import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GitlabApiClient {

    void registerPushWebhook(String gitlabPersonalAccessToken, Long projectId, String hookUrl, String pushEventsBranchFilter);

    void submitCommit(String gitlabPersonalAccessToken, Long projectId, String branch, String commitMessage, List<CommitAction> action);

    MergeRequestCreateResponse submitMergeRequest(String gitlabPersonalAccessToken,
                                                  Long projectId,
                                                  String sourceBranch,
                                                  String targetBranch,
                                                  String title,
                                                  String description);

    GitlabBranch submitBranchCreation(String gitlabPersonalAccessToken, Long projectId, String branch, String ref);

    void submitBranchDeletion(String gitlabPersonalAccessToken, Long projectId, String branch);

    List<GitlabProject> requestProjectList(String gitlabPersonalAccessToken, int page, int perPage);

    GitlabProject requestProjectInfo(String gitlabPersonalAccessToken, String projectUrl);

    Mono<List<GitlabMergeRequest>> requestMergedMrs(String gitlabPersonalAccessToken, Long projectId, int page, int perPage);

    Mono<GitlabMergeRequest> requestMrDetail(String gitlabPersonalAccessToken, Long projectId, Long mergeRequestIid);

    Mono<GitlabCompareResponse> requestCommitComparison(String gitlabPersonalAccessToken, Long projectId, String from, String to);


    List<GitlabTree> requestRepositoryTree(
            String gitlabPersonalAccessToken,
            Long projectId,
            String path,
            boolean recursive,
            int page,
            int perPage,
            String branchName
    );

    String requestRawFileContent(String gitlabPersonalAccessToken, Long projectId, String path, String ref);

    void validateBranchExists(String gitlabPersonalAccessToken, Long projectId, String branchName);

}
