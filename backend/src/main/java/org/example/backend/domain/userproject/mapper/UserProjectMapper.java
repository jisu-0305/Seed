package org.example.backend.domain.userproject.mapper;

import org.example.backend.controller.response.userproject.UserProjectListResponse;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.userproject.dto.UserInProjectWithInvitationStatus;
import org.example.backend.domain.userproject.entity.Invitation;
import org.example.backend.domain.userproject.enums.InvitationStateType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserProjectMapper {

    public static UserProjectListResponse toListResponse(Long projectId, List<User> users, List<Invitation> invitations, Long projectOwnerId) {


        Map<Long, InvitationStateType> stateByUser = invitations.stream()
                .collect(Collectors.toMap(
                        Invitation::getReceiverId,
                        Invitation::getState,
                        (existing, replacement) -> replacement
                ));

        List<UserInProjectWithInvitationStatus> members = users.stream()
                .map(user -> {
                    InvitationStateType state;
                    if ( user.getId().equals(projectOwnerId) ) {
                        state = InvitationStateType.OWNER;
                    } else {
                        state = stateByUser.getOrDefault( user.getId(), InvitationStateType.PENDING );
                    }

                    return UserInProjectWithInvitationStatus.builder()
                            .userId(user.getId())
                            .userName(user.getUserName())
                            .userIdentifyId(user.getUserIdentifyId())
                            .profileImageUrl(user.getProfileImageUrl())
                            .status(state)
                            .build();
                })
                .toList();

        return UserProjectListResponse.builder()
                .projectId(projectId)
                .users(members)
                .build();
    }

}
