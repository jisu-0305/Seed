package org.example.backend.controller.request.userproject;

import lombok.Getter;

import java.util.List;

@Getter
public class InvitationRequest {
    private Long projectId;
    private List<Long> idList;
}
