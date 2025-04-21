package org.example.backend.domain.userproject.service;

import org.example.backend.controller.request.userProject.InvitationRequest;
import org.example.backend.controller.response.userproject.InvitationResponse;

import java.util.List;

public interface InvitationService {
    InvitationResponse sendInvitation(InvitationRequest request, String accessToken);
    void acceptInvitation(Long invitationId, String accessToken);
    void rejectInvitation(Long invitationId, String accessToken);
    List<InvitationResponse> getReceivedInvitations(String accessToken);
}