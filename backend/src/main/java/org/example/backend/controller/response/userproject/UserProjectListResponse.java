package org.example.backend.controller.response.userproject;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserProjectListResponse {
    private Long projectId;
    private List<Long> userIdList;
}
