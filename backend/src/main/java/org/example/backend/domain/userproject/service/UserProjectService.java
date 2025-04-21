package org.example.backend.domain.userproject.service;

import org.example.backend.controller.response.userproject.UserProjectListResponse;


public interface UserProjectService {
    UserProjectListResponse getUserIdListByProjectId(Long projectId, String accessToken);
}
