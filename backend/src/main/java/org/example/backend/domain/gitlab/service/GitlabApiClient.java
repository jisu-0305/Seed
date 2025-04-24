package org.example.backend.domain.gitlab.service;

import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.GitlabBranch;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabTree;

import java.util.List;

public interface GitlabApiClient {

    List<GitlabProject> listProjects(String pat);

    List<GitlabTree> listTree(String pat, Long projectId, String path, boolean recursive);

    String getRawFile(String pat, Long projectId, String path, String ref);

    GitlabProject getProjectInfo(String pat, String projectUrl);

    GitlabCompareResponse compareCommits(String accessToken, Long projectId, String from, String to);

    GitlabBranch createBranch(String accessToken, Long projectId, String branch, String ref);

    void createProjectHook(String privateToken, Long projectId, String hookUrl, String pushEventsBranchFilter);

    void deleteBranch(String accessToken, Long projectId, String branch);

    MergeRequestCreateResponse createMergeRequest(
            String accessToken,
            Long projectId,
            String sourceBranch,
            String targetBranch,
            String title,
            String description
    );

    GitlabBranch getBranch(String accessToken, Long projectId, String branchName);
}
