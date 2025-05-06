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
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/gitlab")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gitlab", description = "깃랩 API")
public class GitlabController {

    private final GitlabService gitlabService;

    /* 1. Push _ webhook 생성 */
    @PostMapping("/projects/{projectId}/hooks")
    @Operation(summary = "깃랩 웹훅_push", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Void>> createPushWebhook(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @Validated @RequestBody ProjectHookRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        gitlabService.createPushWebhook(accessToken, projectId, request.url(), request.wildcard());
        return ResponseEntity.ok(ApiResponse.success());

    }

    /* 2. Push 트리거 */
    @PostMapping("/projects/{projectId}/triggers/push")
    @Operation(summary = "push 트리거", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Void>> triggerPushEvent(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @RequestParam String branch,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        gitlabService.triggerPushEvent(accessToken, projectId, branch);
        return ResponseEntity.ok(ApiResponse.success());

    }

    /* 3. MR생성 */
    @PostMapping("/projects/{projectId}/merge-requests")
    @Operation(summary = "MR 생성", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<MergeRequestCreateResponse>> createMergeRequest(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @Valid @RequestBody CreateMrRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        MergeRequestCreateResponse created = gitlabService.createMergeRequest(
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

    /*4. 브랜치 생성*/
    @PostMapping("/projects/{projectId}/branches")
    @Operation(summary = "새 브랜치 생성", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<GitlabBranch>> createBranch(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @Valid @RequestBody CreateBranchRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        GitlabBranch created = gitlabService.createBranch(accessToken, projectId, request.branch(), request.baseBranch());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));

    }

    /* 5. 브랜치 삭제 */
    @DeleteMapping("/projects/{projectId}/branches")
    @Operation(summary = "브랜치 삭제", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<String>> deleteBranch(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @RequestParam("branch") String branch,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        gitlabService.deleteBranch(accessToken, projectId, branch);
        return ResponseEntity.ok(ApiResponse.success(branch));

    }

    /* 6. 레포지토리 목록 조회 */
    @GetMapping("/projects")
    @Operation(summary = "레포지토리 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<GitlabProject>>> getProjects(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        List<GitlabProject> projects = gitlabService.getProjects(accessToken);
        return ResponseEntity.ok(ApiResponse.success(projects));

    }

    /* 7. 레포지토리 단건 조회 (URL) */
    @GetMapping(value = "/projects", params = "repoUrl")
    @Operation(summary = "레포지토리 조회",security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<GitlabProject>> getProjectByUrl(
            @Parameter(description = "조회할 레포지토리 URL") @RequestParam(name = "repoUrl", required = false) String repoUrl,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        GitlabProject projectInfo = gitlabService.getProjectByUrl(accessToken, repoUrl);
        return ResponseEntity.ok(ApiResponse.success(projectInfo));

    }

    /* 8. Diff 1 ) 최신 MR 기준 diff 조회 */
    @GetMapping("/projects/{projectId}/merge-requests/latest/diff")
    @Operation(summary = "최신 mr 기준 diff 조회",
            description = "프로젝트 id로 해당 프로젝트의 최신 mr diff 가져오기",
            security = @SecurityRequirement(name = "JWT"))
    public Mono<ResponseEntity<ApiResponse<GitlabCompareResponse>>> fetchLatestMrDiff(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long projectId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        return gitlabService.fetchLatestMrDiff(accessToken, projectId)
                .map(ApiResponse::success)
                .map(ResponseEntity::ok);
    }

    /* 9. Diff 2 ) 커밋 간 변경사항 조회 (from-to) */
    @GetMapping("/projects/{projectId}/diff")
    @Operation(summary = "커밋 간 변경사항 조회", security = @SecurityRequirement(name = "JWT"))
    public Mono<ResponseEntity<ApiResponse<GitlabCompareResponse>>> compareCommits(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long projectId,
            @ParameterObject @Valid @ModelAttribute DiffCommitRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        return gitlabService.compareCommits(accessToken, projectId, request.from(), request.to())
                .map(ApiResponse::success)
                .map(ResponseEntity::ok);
    }

    /* 10. 레포지토리 tree 구조 조회  */
    @GetMapping("/projects/{projectId}/tree")
    @Operation(summary = "레포지토리 tree 구조 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<GitlabTree>>> getRepositoryTree(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @ParameterObject @ModelAttribute TreeRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        List<GitlabTree> tree = gitlabService.getRepositoryTree(accessToken, projectId, request.path(), request.recursive());
        return ResponseEntity.ok(ApiResponse.success(tree));

    }

    /* 11. 파일 원본 조회  */
    @GetMapping("/projects/{projectId}/file")
    @Operation(summary = "파일 원본 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<String>> getRawFileContent(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long projectId,
            @ParameterObject @Validated @ModelAttribute ReadFileRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        String content = gitlabService.getRawFileContent(accessToken, projectId, request.filePath(), request.branch());
        return ResponseEntity.ok(ApiResponse.success(content));

    }

}
