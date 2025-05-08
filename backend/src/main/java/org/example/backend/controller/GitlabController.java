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
    @Operation(summary = "깃랩 웹훅_push")
    public ResponseEntity<ApiResponse<Void>> createPushWebhook(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @Validated @RequestBody ProjectHookRequest request,
            String gitlabPersonalAccessToken) {

        gitlabService.createPushWebhook(gitlabPersonalAccessToken, projectId, request.url(), request.wildcard());
        return ResponseEntity.ok(ApiResponse.success());

    }

    /* 2. Push 트리거 */
    @PostMapping("/projects/{projectId}/triggers/push")
    @Operation(summary = "push 트리거")
    public ResponseEntity<ApiResponse<Void>> triggerPushEvent(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @RequestParam String branch,
            String gitlabPersonalAccessToken) {

        gitlabService.triggerPushEvent(gitlabPersonalAccessToken, projectId, branch);
        return ResponseEntity.ok(ApiResponse.success());

    }

    /* 3. MR생성 */
    @PostMapping("/projects/{projectId}/merge-requests")
    @Operation(summary = "MR 생성")
    public ResponseEntity<ApiResponse<MergeRequestCreateResponse>> createMergeRequest(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @Valid @RequestBody CreateMrRequest request,
            String gitlabPersonalAccessToken) {

        MergeRequestCreateResponse created = gitlabService.createMergeRequest(
                gitlabPersonalAccessToken,
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
    @Operation(summary = "새 브랜치 생성")
    public ResponseEntity<ApiResponse<GitlabBranch>> createBranch(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @Valid @RequestBody CreateBranchRequest request,
            String gitlabPersonalAccessToken) {

        GitlabBranch created = gitlabService.createBranch(gitlabPersonalAccessToken, projectId, request.branch(), request.baseBranch());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));

    }

    /* 5. 브랜치 삭제 */
    @DeleteMapping("/projects/{projectId}/branches")
    @Operation(summary = "브랜치 삭제")
    public ResponseEntity<ApiResponse<String>> deleteBranch(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @RequestParam("branch") String branch,
            String gitlabPersonalAccessToken) {

        gitlabService.deleteBranch(gitlabPersonalAccessToken, projectId, branch);
        return ResponseEntity.ok(ApiResponse.success(branch));

    }

    /* 6. 레포지토리 목록 조회 */
    @GetMapping("/projects")
    @Operation(summary = "레포지토리 조회")
    public ResponseEntity<ApiResponse<List<GitlabProject>>> getProjects(String gitlabPersonalAccessToken) {
        List<GitlabProject> projects = gitlabService.getProjects(gitlabPersonalAccessToken);
        return ResponseEntity.ok(ApiResponse.success(projects));

    }

    /* 7. 레포지토리 단건 조회 (URL) */
    @GetMapping(value = "/projects", params = "repoUrl")
    @Operation(summary = "레포지토리 조회")
    public ResponseEntity<ApiResponse<GitlabProject>> getProjectByUrl(
            @Parameter(description = "조회할 레포지토리 URL") @RequestParam(name = "repoUrl", required = false) String repoUrl,
            String gitlabPersonalAccessToken) {

        GitlabProject projectInfo = gitlabService.getProjectByUrl(gitlabPersonalAccessToken, repoUrl);
        return ResponseEntity.ok(ApiResponse.success(projectInfo));

    }

    /* 8. Diff 1 ) 최신 MR 기준 diff 조회 */
    @GetMapping("/projects/{projectId}/merge-requests/latest/diff")
    @Operation(summary = "최신 mr 기준 diff 조회",
            description = "프로젝트 id로 해당 프로젝트의 최신 mr diff 가져오기",
            security = @SecurityRequirement(name = "JWT"))
    public Mono<ResponseEntity<ApiResponse<GitlabCompareResponse>>> fetchLatestMrDiff(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long projectId,
            String gitlabPersonalAccessToken) {

        return gitlabService.fetchLatestMrDiff(gitlabPersonalAccessToken, projectId)
                .map(ApiResponse::success)
                .map(ResponseEntity::ok);
    }

    /* 9. Diff 2 ) 커밋 간 변경사항 조회 (from-to) */
    @GetMapping("/projects/{projectId}/diff")
    @Operation(summary = "커밋 간 변경사항 조회", security = @SecurityRequirement(name = "JWT"))
    public Mono<ResponseEntity<ApiResponse<GitlabCompareResponse>>> compareCommits(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long projectId,
            @ParameterObject @Valid @ModelAttribute DiffCommitRequest request,
            String gitlabPersonalAccessToken) {

        return gitlabService.compareCommits(gitlabPersonalAccessToken, projectId, request.from(), request.to())
                .map(ApiResponse::success)
                .map(ResponseEntity::ok);
    }

    /* 10. 레포지토리 tree 구조 조회  */
    @GetMapping("/projects/{projectId}/tree")
    @Operation(summary = "레포지토리 tree 구조 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<GitlabTree>>> getRepositoryTree(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @ParameterObject @ModelAttribute TreeRequest request,
            String gitlabPersonalAccessToken) {

        List<GitlabTree> tree = gitlabService.getRepositoryTree(
                gitlabPersonalAccessToken,
                projectId,
                request.path(),
                request.recursive(),
                request.branchName()
        );

        return ResponseEntity.ok(ApiResponse.success(tree));

    }

    /* 11. 파일 원본 조회  */
    @GetMapping("/projects/{projectId}/file")
    @Operation(summary = "파일 원본 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<String>> getRawFileContent(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245") @PathVariable Long projectId,
            @ParameterObject @Validated @ModelAttribute ReadFileRequest request,
            String gitlabPersonalAccessToken) {

        String content = gitlabService.getRawFileContent(gitlabPersonalAccessToken, projectId, request.filePath(), request.branch());
        return ResponseEntity.ok(ApiResponse.success(content));

    }

    /* ai 자동수정된 파일들을 한 번에 커밋 */
    @PostMapping("/projects/{projectId}/files/patch")
    @Operation(
            summary = "AI 자동 수정 파일 일괄 커밋",
            description = "AI가 생성한 patchedFiles 목록을 단일 커밋으로 저장소에 반영합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    public ResponseEntity<ApiResponse<Void>> commitPatchedFiles(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708")
            @PathVariable Long projectId,
            @RequestBody CommitPatchedFilesRequest request,
            String gitlabPersonalAccessToken) {

        gitlabService.commitPatchedFiles(gitlabPersonalAccessToken, projectId, request.branch(), request.commitMessage(), request.patchedFiles());
        return ResponseEntity.ok(ApiResponse.success());

    }

}
