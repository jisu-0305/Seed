package org.example.backend.domain.userproject.mapper;

import org.example.backend.controller.response.userproject.UserProjectListResponse;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.userproject.dto.UserInProjectWithInvitationStatus;
import org.example.backend.domain.userproject.entity.Invitation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserProjectMapper {

    public static UserProjectListResponse toListResponse(
            Long projectId,
            List<User> userList,
            List<Invitation> invitations
    ) {
        // receiverId → state 이름 매핑
        Map<Long, String> statusMap = invitations.stream()
                .collect(Collectors.toMap(
                        Invitation::getReceiverId,
                        inv -> inv.getState().name()
                ));

        List<UserInProjectWithInvitationStatus> users = userList.stream()
                .map(u -> UserInProjectWithInvitationStatus.builder()
                        .userId(u.getId())
                        .userName(u.getUserName())
                        .userIdentifyId(u.getUserIdentifyId())
                        .profileImageUrl(u.getProfileImageUrl())
                        .status(statusMap.getOrDefault(u.getId(), "UNKNOWN"))
                        .build())
                .toList();

        return UserProjectListResponse.builder()
                .projectId(projectId)
                .users(users)
                .build();
    }
}
