package org.example.backend.domain.gitlab.service;

import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabTree;

import java.util.List;

public interface GitlabApiClient {

    List<GitlabProject> listProjects(String pat);

    List<GitlabTree> listTree(String pat, Long projectId, String path, boolean recursive);

    String getRawFile(String pat, Long projectId, String path, String ref);

    GitlabCompareResponse compareCommits(String accessToken, Long projectId, String from, String to);

}
