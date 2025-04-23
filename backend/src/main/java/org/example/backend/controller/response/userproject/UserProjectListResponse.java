package org.example.backend.controller.response.userproject;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.userproject.dto.UserInProject;

import java.util.List;

@Getter
@Builder
public class UserProjectListResponse {
    private Long projectId;
    private List<UserInProject> users;
}
