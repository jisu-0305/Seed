package org.example.backend.domain.userproject.service;

import org.example.backend.controller.response.userproject.UserProjectListResponse;


public interface UserProjectService {
    UserProjectListResponse getUserIdsByProjectId(Long projectId, String accessToken);
}
