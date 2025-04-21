package org.example.backend.domain.userproject.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.response.userproject.UserProjectListResponse;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.example.backend.domain.userproject.mapper.UserProjectMapper.toListResponse;

@Service
@RequiredArgsConstructor
public class UserProjectServiceImpl implements UserProjectService {

    private final UserProjectRepository userProjectRepository;
    private final RedisSessionManager redisSessionManager;

    @Override
    public UserProjectListResponse getUserIdsByProjectId(Long projectId, String accessToken) {
        String jwtToken = accessToken.replace("Bearer", "").trim();
        SessionInfoDto session = redisSessionManager.getSession(jwtToken);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = session.getUserId();

        boolean isMember = userProjectRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.FORBIDDEN); // 혹은 ErrorCode.NOT_PROJECT_MEMBER 같은 코드로 구체화 가능
        }

        List<Long> userIds = userProjectRepository.findByProjectId(projectId).stream()
                .map(userProject -> userProject.getUserId())
                .toList();

        return toListResponse(projectId, userIds);
    }
}
