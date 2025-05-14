package org.example.backend.domain.userproject.mapper;

import org.example.backend.controller.response.userproject.InvitationResponse;
import org.example.backend.domain.userproject.entity.Invitation;
import org.example.backend.domain.fcm.enums.NotificationType;

public class InvitationMapper {

    public static InvitationResponse toResponse(Invitation invitation, NotificationType notificationType) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .projectId(invitation.getProjectId())
                .receiverId(invitation.getReceiverId())
                .expiresAt(invitation.getExpiresAt())
                .state(invitation.getState())
                .notificationType(notificationType)
                .build();
    }
}
