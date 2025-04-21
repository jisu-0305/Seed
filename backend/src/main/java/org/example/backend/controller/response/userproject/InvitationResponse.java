package org.example.backend.controller.response.userproject;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InvitationResponse {
    private Long id;
    private Long projectId;
    private Long receiverId;
    private LocalDateTime expiresAt;
}
