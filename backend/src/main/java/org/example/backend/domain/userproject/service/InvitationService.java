package org.example.backend.domain.userproject.service;

import org.example.backend.controller.request.userproject.InvitationRequest;
import org.example.backend.controller.response.userproject.InvitationResponse;
import org.example.backend.domain.userproject.dto.UserInProject;

import java.util.List;

public interface InvitationService {
    List<InvitationResponse> sendInvitations(InvitationRequest request, String accessToken);
    void acceptInvitation(Long invitationId, String accessToken);
    void rejectInvitation(Long invitationId, String accessToken);
    List<InvitationResponse> getReceivedInvitations(String accessToken);
    List<UserInProject> getInvitableUsers(Long projectId, String keyword, String accessToken);
}