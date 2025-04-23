package org.example.backend.domain.userproject.mapper;

import org.example.backend.controller.response.userproject.UserProjectListResponse;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.userproject.dto.UserInProject;

import java.util.List;

public class UserProjectMapper {

    public static UserProjectListResponse toListResponse(Long projectId, List<User> userList) {
        List<UserInProject> users = userList.stream()
                .map(user -> UserInProject.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .username(user.getUsername())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .toList();

        return UserProjectListResponse.builder()
                .projectId(projectId)
                .users(users)
                .build();
    }
}
