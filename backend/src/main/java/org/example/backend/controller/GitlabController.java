package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gitlab")
@RequiredArgsConstructor
@Slf4j
public class GitlabController {

    private final GitlabService gitlabService;
    private final RedisSessionManager sessionManager;

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<GitlabProject>>> listProjects(
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        SessionInfoDto session = sessionManager.getSession(token);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<GitlabProject> projects = gitlabService.getProjects(session.getUserId());
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/projects/{id}/tree")
    public ResponseEntity<ApiResponse<List<GitlabTree>>> listTree(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String path,
            @RequestParam(defaultValue = "true") boolean recursive,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        SessionInfoDto session = sessionManager.getSession(token);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<GitlabTree> tree = gitlabService.getTree(
                session.getUserId(), id, path, recursive);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/projects/{id}/file")
    public ResponseEntity<ApiResponse<String>> getFile(
            @PathVariable Long id,
            @RequestParam String path,
            @RequestParam(defaultValue = "main") String ref,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        SessionInfoDto session = sessionManager.getSession(token);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String content = gitlabService.getFile(
                session.getUserId(), id, path, ref);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    private String extractToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.INVALID_AUTHORIZATION_HEADER);
        }
        return header.substring(7);
    }
}
