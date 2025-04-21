package org.example.backend.controller.request.userProject;

import lombok.Getter;

@Getter
public class InvitationRequest {
    private Long projectId;
    private Long receiverId;
}
