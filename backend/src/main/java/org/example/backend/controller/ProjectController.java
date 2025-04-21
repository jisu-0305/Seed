package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.project.ProjectCreateRequest;
import org.example.backend.controller.response.project.ProjectResponse;
import org.example.backend.domain.project.service.ProjectService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "프로젝트 관리 API")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 등록합니다.")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@RequestBody ProjectCreateRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "프로젝트 단건 조회", description = "ID로 프로젝트 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable Long id) {
        ProjectResponse response = projectService.getProject(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "프로젝트 목록 조회", description = "모든 프로젝트 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects() {
        List<ProjectResponse> responses = projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "프로젝트 삭제", description = "ID에 해당하는 프로젝트를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
