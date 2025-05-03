package org.example.backend.controller;

import com.google.firestore.v1.CommitResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.gitlab.ProjectHookRequest;
import org.example.backend.controller.request.gitlab.ProjectUrlRequest;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.controller.response.gitlab.MergeRequestCreateResponse;
import org.example.backend.domain.gitlab.dto.GitlabBranch;
import org.example.backend.domain.gitlab.dto.GitlabProject;
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
    @Operation(summary = "레포지토리 url 로 레포지토리 단건 조회", security = @SecurityRequirement(name = "JWT"))
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

    @DeleteMapping("/{projectId}/branches")
    @Operation(summary = "브랜치 삭제", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<String>> deleteBranch(
            @PathVariable Long projectId,
            @RequestParam("branch") String branch,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        gitlabService.deleteBranch(accessToken, projectId, branch);

        return ResponseEntity.ok(ApiResponse.success(branch));
    }

    @PostMapping("/{projectId}/merge-requests")
    @Operation(summary = "Merge Request 생성", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<MergeRequestCreateResponse>> createMergeRequest(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708") @PathVariable Long projectId,
            @Parameter(description = "from 브랜치") @RequestParam("sourceBranch")   String sourceBranch,
            @Parameter(description = "to 브랜치",example = "dev") @RequestParam("targetBranch")   String targetBranch,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        MergeRequestCreateResponse created =
                gitlabService.createMergeRequest(accessToken, projectId, sourceBranch, targetBranch, title, description);

        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PostMapping("/projects/hooks")
    @Operation(summary = "깃랩 웹훅_push", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Void>> createWebhook(
            @Validated @RequestBody ProjectHookRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        gitlabService.createPushWebhook(accessToken, request.getProjectId(), request.getUrl(), request.getWildcard());

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{projectId}/merge-requests/latest/diff")
    @Operation(
            summary = "최신 mr 기준 diff 조회",
            description = "프로젝트 id로 해당 프로젝트의 최신 mr diff 가져오기",
            security = @SecurityRequirement(name = "JWT")
    )
    public ResponseEntity<ApiResponse<GitlabCompareResponse>> getLatestMergeRequestDiff(
            @Parameter(description = "프로젝트 ID", required = true, example = "997245")
            @PathVariable Long projectId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
            String accessToken
    ) {

        GitlabCompareResponse diff = gitlabService.getLatestMergeRequestDiff(accessToken, projectId);
        return ResponseEntity.ok(ApiResponse.success(diff));
    }

    @PostMapping("/projects/{projectId}/trigger-push")
    public ResponseEntity<ApiResponse<Void>> triggerPush(
            @Parameter(description = "프로젝트 ID", required = true, example = "998708")
            @PathVariable Long projectId,
            @RequestParam String branch,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
            String accessToken
    ) {
        gitlabService.triggerPushEvent(accessToken, projectId, branch);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
