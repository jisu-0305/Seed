package org.example.backend.domain.gitlab.service;

import org.example.backend.controller.response.gitlab.CommitResponse;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.GitlabProjectListResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GitlabService {

    void createPushWebhook(String gitlabPersonalAccessToken, Long gitlabProjectId, String hookUrl, String branchFilter);

    String triggerPushEvent(String gitlabPersonalAccessToken, Long gitlabProjectId, String branch);

    MergeRequestCreateResponse createMergeRequest(String gitlabPersonalAccessToken, Long gitlabProjectId,
                                                  String sourceBranch, String targetBranch,
                                                  String title, String description);

    GitlabBranch createBranch(String gitlabPersonalAccessToken, Long gitlabProjectId, String branch, String ref);

    void deleteBranch(String gitlabPersonalAccessToken, Long gitlabProjectId, String branch);

    List<GitlabProject> getProjects(String gitlabPersonalAccessToken);

    GitlabProjectListResponse getProjectsByCursor(String gitlabPersonalAccessToken, Long lastProjectId);

    GitlabProject getProjectByUrl(String gitlabPersonalAccessToken, String request);

    GitlabProjectListResponse getGitlabProjectsByUserIdAndCursor(Long userId, Long lastProjectId);

    Mono<GitlabCompareResponse> fetchLatestMrDiff(String gitlabPersonalAccessToken, Long gitlabProjectId);

    Mono<GitlabCompareResponse> compareCommits(String gitlabPersonalAccessToken, Long gitlabProjectId, String from, String to);

    List<GitlabTree> getRepositoryTree(String gitlabPersonalAccessToken, Long gitlabProjectId,
                                        String path, boolean recursive, String branchName);

    String getRawFileContent(String gitlabPersonalAccessToken, Long gitlabProjectId, String path, String ref);

    CommitResponse commitPatchedFiles(String gitlabPersonalAccessToken, Long gitlabProjectId,
                                      String branch, String commitMessage, List<PatchedFile> patchedFiles);

    List<GitlabProject> getGitlabProjectsByUserId(Long userId);

}
