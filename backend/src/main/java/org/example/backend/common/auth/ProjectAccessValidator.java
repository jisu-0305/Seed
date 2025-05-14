package org.example.backend.common.auth;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectAccessValidator {

    private final RedisSessionManager redisSessionManager;
    private final UserProjectRepository userProjectRepository;

    public void validateUserInProject(Long projectId, String jwtToken) {
        SessionInfoDto session = redisSessionManager.getSession(jwtToken);
        Long userId = session.getUserId();
        if (!userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }
    }
}
