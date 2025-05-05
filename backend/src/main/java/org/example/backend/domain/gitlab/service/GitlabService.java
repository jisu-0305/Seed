package org.example.backend.domain.gitlab.service;

import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.*;

import java.util.List;

public interface GitlabService {

    List<GitlabProject> getProjects(String accessToken);

    List<GitlabTree> getTree(String accessToken, Long projectId, String path, boolean recursive);

    String getFile(String accessToken, Long projectId, String path, String ref);

    GitlabProject getProjectInfo(String accessToken, String request);

    GitlabCompareResponse getDiff(String accessToken, Long projectId, String from, String to);

    GitlabBranch createBranch(String accessToken, Long projectId, String branch, String ref);

    void deleteBranch(String accessToken, Long projectId, String branch);

    MergeRequestCreateResponse createMergeRequest(
            String accessToken,
            Long projectId,
            String sourceBranch,
            String targetBranch,
            String title,
            String description
    );

    void createPushWebhook(String accessToken, Long projectId, String hookUrl, String branchFilter);

    GitlabCompareResponse getLatestMergeRequestDiff(String accessToken, Long projectId);

    void triggerPushEvent(String authHeader, Long projectId, String branch);

}
