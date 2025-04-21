package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.response.userproject.UserProjectListResponse;
import org.example.backend.domain.userproject.service.UserProjectService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/user-projects")
@RequiredArgsConstructor
@Tag(name = "UserProject", description = "유저-프로젝트 매핑 API")
public class UserProjectController {

    private final UserProjectService userProjectService;

    @GetMapping("/project/{projectId}")
    @Operation(summary = "프로젝트 참여자 목록 조회", description = "프로젝트 ID로 참여한 사용자 ID들을 조회합니다.", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<UserProjectListResponse>> getUsersByProjectId(@PathVariable Long projectId, @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        UserProjectListResponse result = userProjectService.getUserIdsByProjectId(projectId, accessToken);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
