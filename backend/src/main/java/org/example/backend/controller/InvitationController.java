package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.userProject.InvitationRequest;
import org.example.backend.controller.response.userproject.InvitationResponse;
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
    @Operation(summary = "프로젝트 초대 생성", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<InvitationResponse>> sendInvitation(
            @RequestBody InvitationRequest request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        InvitationResponse response = invitationService.sendInvitation(request, accessToken);
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
}
