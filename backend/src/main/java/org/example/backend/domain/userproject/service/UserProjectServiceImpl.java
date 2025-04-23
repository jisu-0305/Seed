package org.example.backend.domain.userproject.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.response.userproject.UserProjectListResponse;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.domain.userproject.entity.UserProject;
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
    private final UserRepository userRepository;

    @Override
    public UserProjectListResponse getUserIdListByProjectId(Long projectId, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        if (!userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }

        List<Long> userIdList = userProjectRepository.findByProjectId(projectId).stream()
                .map(UserProject::getUserId)
                .toList();

        List<User> users = userRepository.findAllById(userIdList);

        return toListResponse(projectId, users);
    }
}
