package org.example.backend.domain.gitlab.service;

import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GitlabService {

    void createPushWebhook(String accessToken, Long projectId, String hookUrl, String branchFilter);

    void triggerPushEvent(String accessToken, Long projectId, String branch);

    MergeRequestCreateResponse createMergeRequest(String accessToken,
                                                  Long projectId,
                                                  String sourceBranch,
                                                  String targetBranch,
                                                  String title,
                                                  String description);

    GitlabBranch createBranch(String accessToken, Long projectId, String branch, String ref);

    void deleteBranch(String accessToken, Long projectId, String branch);

    List<GitlabProject> getProjects(String accessToken);

    GitlabProject getProjectByUrl(String accessToken, String request);

    Mono<GitlabCompareResponse> fetchLatestMrDiff(String accessToken, Long projectId);

    Mono<GitlabCompareResponse> compareCommits(String accessToken, Long projectId, String from, String to);

    List<GitlabTree> getRepositoryTree(String accessToken, Long projectId, String path, boolean recursive);

    String getRawFileContent(String accessToken, Long projectId, String path, String ref);

    void commitPatchedFiles(String accessToken, Long projectId, String branch, String commitMessage, List<PatchedFile> patchedFiles);

}
