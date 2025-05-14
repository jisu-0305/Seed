package org.example.backend.domain.userproject.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.response.userproject.UserProjectListResponse;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.domain.userproject.entity.Invitation;
import org.example.backend.domain.userproject.entity.UserProject;
import org.example.backend.domain.userproject.mapper.UserProjectMapper;
import org.example.backend.domain.userproject.repository.InvitationRepository;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserProjectServiceImpl implements UserProjectService {

    private final UserProjectRepository userProjectRepository;
    private final ProjectRepository projectRepository;
    private final InvitationRepository invitationRepository;
    private final RedisSessionManager redisSessionManager;
    private final UserRepository userRepository;

    @Override
    public UserProjectListResponse getUserIdListByProjectId(Long projectId, String accessToken) {

        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long currentUserId = session.getUserId();

        if (!userProjectRepository.existsByProjectIdAndUserId(projectId, currentUserId)) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }

        Long projectOwnerId = projectRepository.findById(projectId).get().getOwnerId();

        List<Long> memberIds = userProjectRepository.findByProjectId(projectId).stream()
                .map(UserProject::getUserId)
                .toList();

        List<Invitation> invitations = invitationRepository.findAllByProjectId(projectId);

        Set<Long> allUserIds = new LinkedHashSet<>(memberIds);
        invitations.forEach(inv -> allUserIds.add(inv.getReceiverId()));

        List<User> users = userRepository.findAllById(new ArrayList<>(allUserIds));

        return UserProjectMapper.toListResponse(projectId, users, invitations, projectOwnerId);
    }
}
