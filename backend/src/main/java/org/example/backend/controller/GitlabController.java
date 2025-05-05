package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.gitlab.*;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.GitlabBranch;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.global.response.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
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
    @Operation(summary = "레포지토리 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<GitlabProject>>> listProjects(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        List<GitlabProject> projects = gitlabService.getProjects(accessToken);
        return ResponseEntity.ok(ApiResponse.success(projects));

    }

    @GetMapping(value = "/projects", params = "repoUrl")
    @Operation(summary = "레포지토리 URL 쿼리로 단건 조회",security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<GitlabProject>> getProjectInfo(
            @Parameter(description = "조회할 레포지토리 URL") @RequestParam(name = "repoUrl", required = false) String repoUrl,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        GitlabProject projectInfo = gitlabService.getProjectInfo(accessToken, repoUrl);
        return ResponseEntity.ok(ApiResponse.success(projectInfo));

    }

    @GetMapping("/projects/{projectId}/tree")
    @Operation(summary = "레포지토리 tree 구조 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<GitlabTree>>> listTree(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @ParameterObject @ModelAttribute TreeRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        List<GitlabTree> tree = gitlabService.getTree(accessToken, projectId, request.path(), request.recursive());
        return ResponseEntity.ok(ApiResponse.success(tree));

    }

    @GetMapping("/projects/{projectId}/file")
    @Operation(summary = "파일 원본 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<String>> getFile(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long projectId,
            @ParameterObject @Validated @ModelAttribute ReadFileRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        String content = gitlabService.getFile(accessToken, projectId, request.filePath(), request.branch());
        return ResponseEntity.ok(ApiResponse.success(content));

    }

    @GetMapping("/projects/{projectId}/diff")
    @Operation(summary = "커밋 간 변경사항 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<GitlabCompareResponse>> getDiff(
            @PathVariable Long projectId,
            @ParameterObject @Valid @ModelAttribute DiffCommitRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        GitlabCompareResponse diff = gitlabService.getDiff(accessToken, projectId, request.from(), request.to());
        return ResponseEntity.ok(ApiResponse.success(diff));

    }

    @PostMapping("/projects/{projectId}/branches")
    @Operation(summary = "새 브랜치 생성", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<GitlabBranch>> createBranch(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @ParameterObject @Valid @RequestBody CreateBranchRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        GitlabBranch created = gitlabService.createBranch(accessToken, projectId, request.branch(), request.baseBranch());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));

    }

    @DeleteMapping("/projects/{projectId}/branches")
    @Operation(summary = "브랜치 삭제", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<String>> deleteBranch(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @RequestParam("branch") String branch,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        gitlabService.deleteBranch(accessToken, projectId, branch);
        return ResponseEntity.ok(ApiResponse.success(branch));

    }

    @PostMapping("/projects/{projectId}/merge-requests")
    @Operation(summary = "MR 생성", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<MergeRequestCreateResponse>> createMergeRequest(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @ParameterObject @Valid @RequestBody CreateMrRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        MergeRequestCreateResponse created =
                gitlabService.createMergeRequest(
                        accessToken,
                        projectId,
                        request.sourceBranch(),
                        request.targetBranch(),
                        request.title(),
                        request.description()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));

    }

    @PostMapping("/projects/{projectId}/hooks")
    @Operation(summary = "깃랩 웹훅_push", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Void>> createWebhook(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @ParameterObject @Validated @RequestBody ProjectHookRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        gitlabService.createPushWebhook(accessToken, projectId, request.getUrl(), request.getWildcard());
        return ResponseEntity.ok(ApiResponse.success());

    }

    @GetMapping("/projects/{projectId}/merge-requests/latest/diff")
    @Operation(
            summary = "최신 mr 기준 diff 조회",
            description = "프로젝트 id로 해당 프로젝트의 최신 mr diff 가져오기",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<GitlabCompareResponse>> getLatestMergeRequestDiff(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long projectId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        GitlabCompareResponse diff = gitlabService.getLatestMergeRequestDiff(accessToken, projectId);
        return ResponseEntity.ok(ApiResponse.success(diff));

    }

    @PostMapping("/projects/{projectId}/triggers/push")
    public ResponseEntity<ApiResponse<Void>> triggerPush(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @RequestParam String branch,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        gitlabService.triggerPushEvent(accessToken, projectId, branch);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
