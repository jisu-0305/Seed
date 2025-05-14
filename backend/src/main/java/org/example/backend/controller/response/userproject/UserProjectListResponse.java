package org.example.backend.controller.response.userproject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.userproject.dto.UserInProjectWithInvitationStatus;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserProjectListResponse {
    private Long projectId;
    private List<UserInProjectWithInvitationStatus> users;
}
