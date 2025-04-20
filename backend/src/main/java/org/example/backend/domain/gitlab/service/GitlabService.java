package org.example.backend.domain.gitlab.service;

import org.example.backend.domain.gitlab.dto.*;

import java.util.List;

public interface GitlabService {

    void registerToken(Long userId, String token);

    List<GitlabProjectDto> getProjects(Long userId);

    List<GitlabTreeItemDto> getTree(Long userId, Long projectId,
                                    String path, boolean recursive);

    String getFile(Long userId, Long projectId,
                   String path, String ref);
}
