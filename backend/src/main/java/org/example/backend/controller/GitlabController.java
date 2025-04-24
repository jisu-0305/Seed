package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.gitlab.ProjectUrlRequest;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.domain.gitlab.dto.GitlabBranch;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabProjectHook;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gitlab")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gitlab", description = "깃랩 API")
public class GitlabController {

    private final GitlabService gitlabService;

    @GetMapping("/projects")
    @Operation(summary = "전체 레포지토리 목록 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<GitlabProject>>> listProjects(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        List<GitlabProject> projects = gitlabService.getProjects(accessToken);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @PostMapping("/projects")
    @Operation(summary = "레포지토리 url로 레포지토리 단건 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<GitlabProject>> getProjectInfoByUrl(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken,
            @Parameter(
                    description = "조회할 레포지토리 URL",
                    required = true,
                    example = "https://lab.ssafy.com/s12-final/S12P31A206"
            ) @Validated @RequestBody ProjectUrlRequest request) {

        GitlabProject projectInfo = gitlabService.getProjectInfo(accessToken, request);

        return ResponseEntity.ok(ApiResponse.success(projectInfo));
    }

    @GetMapping("/projects/{id}/tree")
    @Operation(summary = "레포지토리 tree 구조 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<GitlabTree>>> listTree(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long id,
            @Parameter(description = "조회할 경로 (빈 문자열이면 루트)") @RequestParam(defaultValue = "") String path,
            @Parameter(description = "하위 디렉토리까지 재귀적으로 조회할지 여부", example = "true") @RequestParam(defaultValue = "true") boolean recursive,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        List<GitlabTree> tree = gitlabService.getTree(accessToken, id, path, recursive);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/projects/{id}/file")
    @Operation(summary = "파일 원본 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<String>> getFile(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long id,
            @Parameter(description = "가져올 파일 경로 (예: src/main/java/…)",
                    required = true,
                    example = "backend/build.gradle"
            ) @RequestParam String path,
            @Parameter(description = "가져올 파일의 브랜치 또는 커밋 SHA (기본값: main)",example = "dev") @RequestParam(defaultValue = "main") String ref,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        String content = gitlabService.getFile(accessToken, id, path, ref);
        return ResponseEntity.ok(ApiResponse.success(content));
    }


    @GetMapping("/diff")
    @Operation(summary = "커밋 간 변경사항 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<GitlabCompareResponse>> getDiff(
            @RequestParam("projectId") Long projectId,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        GitlabCompareResponse diff = gitlabService.getDiff(accessToken, projectId, from, to);

        return ResponseEntity.ok(ApiResponse.success(diff));
    }

    @PostMapping("/{projectId}/branches")
    public ResponseEntity<ApiResponse<GitlabBranch>> createBranch(
            @PathVariable Long projectId,
            @RequestParam("branch") String branch,
            @RequestParam("ref")    String ref,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        GitlabBranch created = gitlabService.createBranch(accessToken, projectId, branch, ref);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /* webhook _ push event */
    @PostMapping("/projects/{projectId}/hooks")
    @Operation(summary = "깃랩 웹훅_push", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Void>> createWebhook(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @Parameter(description = "수행할 url", example = "https://httpbin.org/get?foo=bar") @RequestParam String url,
            @RequestParam(required = false, defaultValue = "") String wildcard,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        gitlabService.createPushWebhook(accessToken, projectId, url, wildcard);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/hook-test")
    public ResponseEntity<ApiResponse<String>> hookTest() {
        log.debug(">>> hook-test endpoint called");
        return ResponseEntity.ok(ApiResponse.success("Hook test successful"));
    }

}
