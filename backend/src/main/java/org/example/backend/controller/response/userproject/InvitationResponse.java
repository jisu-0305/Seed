package org.example.backend.controller.response.userproject;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.fcm.enums.NotificationType;
import org.example.backend.domain.userproject.enums.InvitationStateType;

import java.time.LocalDateTime;

@Getter
@Builder
public class InvitationResponse {
    private Long id;
    private Long projectId;
    private Long receiverId;
    private LocalDateTime expiresAt;
    private NotificationType notificationType;
    private InvitationStateType state;
}
