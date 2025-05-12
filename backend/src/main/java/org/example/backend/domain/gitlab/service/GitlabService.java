package org.example.backend.domain.gitlab.service;

import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GitlabService {

    void createPushWebhook(String gitlabPersonalAccessToken, Long projectId, String hookUrl, String branchFilter);

    void triggerPushEvent(String gitlabPersonalAccessToken, Long projectId, String branch);

    MergeRequestCreateResponse createMergeRequest(String gitlabPersonalAccessToken,
                                                  Long projectId,
                                                  String sourceBranch,
                                                  String targetBranch,
                                                  String title,
                                                  String description);

    GitlabBranch createBranch(String gitlabPersonalAccessToken, Long projectId, String branch, String ref);

    void deleteBranch(String gitlabPersonalAccessToken, Long projectId, String branch);

    List<GitlabProject> getProjects(String gitlabPersonalAccessToken);

    GitlabProject getProjectByUrl(String gitlabPersonalAccessToken, String request);

    Mono<GitlabCompareResponse> fetchLatestMrDiff(String gitlabPersonalAccessToken, Long projectId);

    Mono<GitlabCompareResponse> compareCommits(String gitlabPersonalAccessToken, Long projectId, String from, String to);

    List<GitlabTree> getRepositoryTree(
            String gitlabPersonalAccessToken,
            Long projectId,
            String path,
            boolean recursive,
            String branchName
    );

    String getRawFileContent(String gitlabPersonalAccessToken, Long projectId, String path, String ref);

    void commitPatchedFiles(String gitlabPersonalAccessToken, Long projectId, String branch, String commitMessage, List<PatchedFile> patchedFiles);

    List<GitlabProject> getGitlabProjectsByUserId(Long userId);
}
