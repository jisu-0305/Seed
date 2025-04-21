package org.example.backend.domain.userproject.mapper;

import org.example.backend.controller.response.userproject.UserProjectListResponse;

import java.util.List;

public class UserProjectMapper {

    public static UserProjectListResponse toListResponse(Long projectId, List<Long> userIds) {
        return UserProjectListResponse.builder()
                .projectId(projectId)
                .userIds(userIds)
                .build();
    }
}
