// src/main/java/org/example/backend/domain/gitlab/service/GitlabApiClient.java
package org.example.backend.domain.gitlab.service;

import org.example.backend.domain.gitlab.dto.GitlabProjectDto;
import org.example.backend.domain.gitlab.dto.GitlabTreeItemDto;

import java.util.List;

public interface GitlabApiClient {

    /**
     * 사용자의 Personal Access Token(PAT)으로 가입된 모든 프로젝트 목록을 가져온다.
     *
     * @param pat GitLab Personal Access Token
     * @return 프로젝트 DTO 리스트
     */
    List<GitlabProjectDto> listProjects(String pat);

    /**
     * 특정 프로젝트의 파일/폴더 트리를 가져온다.
     *
     * @param pat        GitLab Personal Access Token
     * @param projectId  프로젝트 ID
     * @param path       조회할 경로 (빈 문자열이면 루트)
     * @param recursive  재귀 조회 여부
     * @return 트리 아이템 DTO 리스트
     */
    List<GitlabTreeItemDto> listTree(String pat, Long projectId, String path, boolean recursive);

    /**
     * 특정 파일의 원문(plain text)을 가져온다.
     *
     * @param pat        GitLab Personal Access Token
     * @param projectId  프로젝트 ID
     * @param path       파일 경로 (URL-encoded 전)
     * @param ref        브랜치/태그/커밋 SHA
     * @return 파일 내용
     */
    String getRawFile(String pat, Long projectId, String path, String ref);

}
