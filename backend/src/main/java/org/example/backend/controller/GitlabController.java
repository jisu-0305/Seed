package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.SessionInfo;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.domain.gitlab.dto.GitlabProjectDto;
import org.example.backend.domain.gitlab.dto.GitlabTreeItemDto;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gitlab")
@RequiredArgsConstructor
public class GitlabController {

    private final GitlabService gitlabService;

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<GitlabProjectDto>>> listProjects(
            @SessionInfo SessionInfoDto session) {

        List<GitlabProjectDto> projects = gitlabService.getProjects(session.getUserId());
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/projects/{id}/tree")
    public ResponseEntity<ApiResponse<List<GitlabTreeItemDto>>> listTree(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String path,
            @RequestParam(defaultValue = "true") boolean recursive, //기본적으로 모든 트리구조 불러오기
            @SessionInfo SessionInfoDto session) {

        List<GitlabTreeItemDto> tree = gitlabService.getTree(
                session.getUserId(), id, path, recursive);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/projects/{id}/file")
    public ResponseEntity<ApiResponse<String>> getFile(
            @PathVariable Long id,
            @RequestParam String path,
            @RequestParam(defaultValue = "main") String ref,
            @SessionInfo SessionInfoDto session) {

        String content = gitlabService.getFile(
                session.getUserId(), id, path, ref);
        return ResponseEntity.ok(ApiResponse.success(content));
    }
}
