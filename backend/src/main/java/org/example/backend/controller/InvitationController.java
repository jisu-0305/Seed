package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.userproject.InvitationRequest;
import org.example.backend.controller.response.userproject.InvitationResponse;
import org.example.backend.domain.userproject.dto.UserInProject;
import org.example.backend.domain.userproject.service.InvitationService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitation", description = "초대 API")
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    @Operation(summary = "다수 사용자 초대", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> sendInvitations(
            @RequestBody InvitationRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        List<InvitationResponse> response = invitationService.sendInvitations(request, accessToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{invitationId}/accept")
    @Operation(summary = "초대 수락", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @PathVariable Long invitationId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        invitationService.acceptInvitation(invitationId, accessToken);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{invitationId}/reject")
    @Operation(summary = "초대 거절", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(
            @PathVariable Long invitationId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        invitationService.rejectInvitation(invitationId, accessToken);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/received")
    @Operation(summary = "내가 받은 초대 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getReceivedInvitations(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        List<InvitationResponse> result = invitationService.getReceivedInvitations(accessToken);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/candidates")
    @Operation(summary = "초대 가능한 사용자 목록 조회", description = "해당 프로젝트에 참여하지 않았고, 나 자신이 아닌 사용자 중 이름으로 검색된 후보를 반환합니다.", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<UserInProject>>> getInvitableUsers(
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        List<UserInProject> result = invitationService.getInvitableUsers(projectId, keyword, accessToken);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
