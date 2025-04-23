package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.gitlab.ProjectUrlRequest;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gitlab")
@RequiredArgsConstructor
@Slf4j
public class GitlabController {

    private final GitlabService gitlabService;

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<GitlabProject>>> listProjects(
            @RequestHeader("Authorization") String accessToken) {

        List<GitlabProject> projects = gitlabService.getProjects(accessToken);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<GitlabProject>> getProjectInfoByUrl(
            @RequestHeader("Authorization") String accessToken,
            @Validated @RequestBody ProjectUrlRequest request) {

        GitlabProject projectInfo = gitlabService.getProjectInfo(accessToken, request);

        return ResponseEntity.ok(ApiResponse.success(projectInfo));
    }


    @GetMapping("/projects/{id}/tree")
    public ResponseEntity<ApiResponse<List<GitlabTree>>> listTree(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String path,
            @RequestParam(defaultValue = "true") boolean recursive,
            @RequestHeader("Authorization") String accessToken) {

        List<GitlabTree> tree = gitlabService.getTree(accessToken, id, path, recursive);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/projects/{id}/file")
    public ResponseEntity<ApiResponse<String>> getFile(
            @PathVariable Long id,
            @RequestParam String path,
            @RequestParam(defaultValue = "main") String ref,
            @RequestHeader("Authorization") String accessToken) {

        String content = gitlabService.getFile(
                accessToken, id, path, ref);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

}
