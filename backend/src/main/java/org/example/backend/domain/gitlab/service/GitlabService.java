package org.example.backend.domain.gitlab.service;

import org.example.backend.domain.gitlab.dto.*;

import java.util.List;

public interface GitlabService {
    List<GitlabProject> getProjects(String accessToken);

    List<GitlabTree> getTree(String accessToken, Long projectId, String path, boolean recursive);

    String getFile(String accessToken, Long projectId, String path, String ref);
}
