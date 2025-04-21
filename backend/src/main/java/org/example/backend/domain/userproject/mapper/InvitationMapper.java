package org.example.backend.domain.userproject.mapper;

import org.example.backend.controller.response.userproject.InvitationResponse;
import org.example.backend.domain.userproject.entity.Invitation;

public class InvitationMapper {

    public static InvitationResponse toResponse(Invitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .projectId(invitation.getProjectId())
                .receiverId(invitation.getReceiverId())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }
}
