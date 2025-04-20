package org.example.backend.domain.gitlab.service;

import org.example.backend.domain.gitlab.dto.GitlabProjectDto;
import org.example.backend.domain.gitlab.dto.GitlabTreeItemDto;

import java.util.List;

public interface GitlabApiClient {
    List<GitlabProjectDto> listProjects(String pat);

    List<GitlabTreeItemDto> listTree(String pat, Long projectId, String path, boolean recursive);

    String getRawFile(String pat, Long projectId, String path, String ref);
}
