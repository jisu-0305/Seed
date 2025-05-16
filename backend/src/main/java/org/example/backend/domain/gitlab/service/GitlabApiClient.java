package org.example.backend.domain.gitlab.service;

import org.example.backend.controller.response.gitlab.CommitResponse;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GitlabApiClient {

    void registerPushWebhook(String gitlabPersonalAccessToken, Long gitlabProjectId, String hookUrl, String pushEventsBranchFilter);

    CommitResponse submitCommit(String gitlabPersonalAccessToken, Long gitlabProjectId, String branch, String commitMessage, List<CommitAction> action);

    MergeRequestCreateResponse submitMergeRequest(String gitlabPersonalAccessToken,
                                                  Long gitlabProjectId,
                                                  String sourceBranch,
                                                  String targetBranch,
                                                  String title,
                                                  String description);

    GitlabBranch submitBranchCreation(String gitlabPersonalAccessToken, Long gitlabProjectId, String branch, String ref);

    void submitBranchDeletion(String gitlabPersonalAccessToken, Long gitlabProjectId, String branch);

    List<GitlabProject> requestProjectList(String gitlabPersonalAccessToken, int page, int perPage);

    List<GitlabProject> requestProjectListBeforeCursor(String gitlabPersonalAccessToken, Long lastProjectId, int pageSize);

    GitlabProject requestProjectInfo(String gitlabPersonalAccessToken, String projectUrl);

    Mono<List<GitlabMergeRequest>> requestMergedMrs(String gitlabPersonalAccessToken, Long gitlabProjectId, int page, int perPage);

    Mono<GitlabMergeRequest> requestMrDetail(String gitlabPersonalAccessToken, Long projectId, Long mergeRequestIid);

    Mono<GitlabCompareResponse> requestCommitComparison(String gitlabPersonalAccessToken, Long gitlabProjectId, String from, String to);


    List<GitlabTree> requestRepositoryTree(
            String gitlabPersonalAccessToken,
            Long gitlabProjectId,
            String path,
            boolean recursive,
            int page,
            int perPage,
            String branchName
    );

    String requestRawFileContent(String gitlabPersonalAccessToken, Long gitlabProjectId, String path, String ref);

    void validateBranchExists(String gitlabPersonalAccessToken, Long gitlabProjectId, String branchName);

}
