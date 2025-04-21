package org.example.backend.domain.gitlab.service;

import org.example.backend.domain.gitlab.dto.*;

import java.util.List;

public interface GitlabService {
    List<GitlabProject> getProjects(Long userId);

    List<GitlabTree> getTree(Long userId, Long projectId, String path, boolean recursive);

    String getFile(Long userId, Long projectId, String path, String ref);
}
