package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.util.SwaggerBody;
import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.response.project.*;
import org.example.backend.domain.project.service.ProjectService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "프로젝트 관리 API")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로젝트 생성", description = "multipart/form-data로 프로젝트 정보 및 파일(.env, .pem)을 업로드합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @SwaggerBody(content = @Content(encoding = @Encoding(name = "projectRequest", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @RequestPart("projectRequest") ProjectCreateRequest request,
            @RequestPart("clientEnvFile") MultipartFile clientEnvFile,
            @RequestPart("serverEnvFile") MultipartFile serverEnvFile,
            @RequestPart("pemFile") MultipartFile pemFile,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        ProjectResponse response = projectService.createProject(request, clientEnvFile, serverEnvFile, pemFile, accessToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "프로젝트 상세 조회", description = "구조, 환경, 어플리케이션 정보를 포함한 상세 정보를 조회합니다.", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProjectDetail(
            @PathVariable Long id,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        ProjectDetailResponse response = projectService.getProjectDetail(id, accessToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "프로젝트 목록 조회", description = "내가 참여한 모든 프로젝트 리스트를 조회합니다.", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        List<ProjectResponse> responses = projectService.getAllProjects(accessToken);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "프로젝트 삭제", description = "내가 참여한 프로젝트만 삭제할 수 있습니다.", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        projectService.deleteProject(id, accessToken);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/executions")
    @Operation(summary = "내 전체 프로젝트 실행 이력 조회", description = "내가 속한 모든 프로젝트의 실행 이력을 날짜별로 그룹화하여 반환합니다.", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<ProjectExecutionGroupResponse>>> getGroupedExecutions(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        List<ProjectExecutionGroupResponse> grouped = projectService.getMyProjectExecutionsGroupedByDate(accessToken);
        return ResponseEntity.ok(ApiResponse.success(grouped));
    }

    @GetMapping("/status")
    @Operation(summary = "내 전체 프로젝트 상태 조회", description = "HTTPS, 자동 배포 여부, 최신 빌드 상태 등을 포함한 내 프로젝트 상태 목록 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<ProjectStatusResponse>>> getMyProjectStatuses(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        List<ProjectStatusResponse> statusList = projectService.getMyProjectStatuses(accessToken);
        return ResponseEntity.ok(ApiResponse.success(statusList));
    }

    @GetMapping("/applications")
    @Operation(summary = "사용 가능한 Application 목록 검색",
            description = "키워드로 applications 를 검색하되, 이미 해당 프로젝트에 등록된 application 은 제외합니다.",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<ProjectApplicationResponse>>> searchApplications(
            @RequestParam String keyword,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        List<ProjectApplicationResponse> apps = projectService.searchAvailableApplications(accessToken, keyword);
        return ResponseEntity.ok(ApiResponse.success(apps));
    }

}
