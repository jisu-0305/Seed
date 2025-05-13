package org.example.backend.domain.userproject.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.userproject.enums.InvitationStateType;

@Getter
@Builder
public class UserInProjectWithInvitationStatus {
    private Long userId;
    private String userName;
    private String userIdentifyId;
    private String profileImageUrl;
    private InvitationStateType status;
}
