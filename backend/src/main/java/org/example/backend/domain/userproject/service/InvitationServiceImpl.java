    package org.example.backend.domain.userproject.service;

    import lombok.RequiredArgsConstructor;
    import org.example.backend.common.session.RedisSessionManager;
    import org.example.backend.common.session.dto.SessionInfoDto;
    import org.example.backend.common.util.TrieSearch;
    import org.example.backend.controller.request.userproject.InvitationRequest;
    import org.example.backend.controller.response.userproject.InvitationResponse;
    import org.example.backend.domain.fcm.service.NotificationService;
    import org.example.backend.domain.fcm.template.NotificationMessageTemplate;
    import org.example.backend.domain.project.repository.ProjectRepository;
    import org.example.backend.domain.userproject.dto.UserInProject;
    import org.example.backend.domain.userproject.entity.Invitation;
    import org.example.backend.domain.userproject.entity.UserProject;
    import org.example.backend.domain.userproject.enums.InvitationStateType;
    import org.example.backend.domain.userproject.mapper.InvitationMapper;
    import org.example.backend.domain.userproject.repository.InvitationRepository;
    import org.example.backend.domain.userproject.repository.UserProjectRepository;
    import org.example.backend.global.exception.BusinessException;
    import org.example.backend.global.exception.ErrorCode;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.example.backend.domain.fcm.enums.NotificationType;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Objects;
    import java.util.Set;
    import java.util.stream.Collectors;

    import static org.example.backend.domain.userproject.mapper.InvitationMapper.toResponse;

    @Service
    @RequiredArgsConstructor
    public class InvitationServiceImpl implements InvitationService {

        private final InvitationRepository invitationRepository;
        private final UserProjectRepository userProjectRepository;
        private final RedisSessionManager redisSessionManager;
        private final NotificationService notificationService;
        private final ProjectRepository projectRepository;

        @Override
        @Transactional
        public List<InvitationResponse> sendInvitations(InvitationRequest request, String accessToken) {
            SessionInfoDto session = redisSessionManager.getSession(accessToken);
            Long senderId = session.getUserId();

            if (!userProjectRepository.existsByProjectIdAndUserId(request.getProjectId(), senderId)) {
                throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
            }

            String projectName = projectRepository.findById(request.getProjectId()).get().getProjectName();

            return request.getIdList().stream()
                    .filter(receiverId -> !Objects.equals(senderId, receiverId))
                    .filter(receiverId -> !invitationRepository.existsByProjectIdAndReceiverId(request.getProjectId(), receiverId))
                    .map(receiverId -> {
                        Invitation invitation = Invitation.create(
                                request.getProjectId(), senderId, receiverId, InvitationStateType.PENDING
                        );
                        Invitation saved = invitationRepository.save(invitation);

                        notificationService.notifyUsers(
                                List.of(receiverId),
                                NotificationMessageTemplate.INVITATION_CREATED,
                                projectName
                        );

                        return toResponse(saved, NotificationType.INVITATION_CREATED_TYPE);
                    })
                    .toList();
        }

        @Override
        @Transactional
        public void acceptInvitation(Long invitationId, String accessToken) {
            SessionInfoDto session = redisSessionManager.getSession(accessToken);
            Long userId = session.getUserId();

            Invitation invitation = invitationRepository.findById(invitationId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVITATION_NOT_FOUND));

            if (!invitation.getReceiverId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            if (userProjectRepository.existsByProjectIdAndUserId(invitation.getProjectId(), userId)) {
                throw new BusinessException(ErrorCode.ALREADY_JOINED_PROJECT);
            }

            UserProject userProject = UserProject.create(invitation.getProjectId(), userId);
            userProjectRepository.save(userProject);

            invitation.accept();
            invitationRepository.save(invitation);

            String projectName = projectRepository.findById(invitation.getProjectId()).get().getProjectName();
            List<Long> otherUserIdList = userProjectRepository.findUserIdsByProjectId(invitation.getProjectId()).stream()
                    .filter(id -> !id.equals(userId))
                    .toList();

            notificationService.notifyUsers(otherUserIdList, NotificationMessageTemplate.INVITATION_ACCEPTED, projectName);
        }

        @Override
        @Transactional
        public void rejectInvitation(Long invitationId, String accessToken) {
            SessionInfoDto session = redisSessionManager.getSession(accessToken);
            Long userId = session.getUserId();

            Invitation invitation = invitationRepository.findById(invitationId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVITATION_NOT_FOUND));

            if (!invitation.getReceiverId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            invitationRepository.delete(invitation);
        }

        @Override
        @Transactional(readOnly = true)
        public List<InvitationResponse> getReceivedInvitations(String accessToken) {
            SessionInfoDto session = redisSessionManager.getSession(accessToken);
            Long receiverId = session.getUserId();

            return invitationRepository
                    .findByReceiverIdAndExpiresAtAfter(receiverId, LocalDateTime.now())
                    .stream()
                    .map(invitation -> InvitationMapper.toResponse(
                            invitation,
                            NotificationType.INVITATION_CREATED_TYPE
                    ))
                    .toList();
        }

        @Override
        public List<UserInProject> getInvitableUsers(Long projectId, String keyword, String accessToken) {
            SessionInfoDto session = redisSessionManager.getSession(accessToken);
            Long currentUserId = session.getUserId();

            List<String> matched;
            if (keyword == null || keyword.trim().isEmpty()) {
                matched = TrieSearch.getAll();
            } else {
                matched = TrieSearch.search(keyword);
            }

            List<UserInProject> allMatched = matched.stream()
                    .map(data -> {
                        String[] parts = data.split("::");
                        if (parts.length < 4) return null; // 데이터 불완전 시 제외
                        return UserInProject.builder()
                                .userId(Long.parseLong(parts[0]))
                                .userIdentifyId(parts[1])
                                .profileImageUrl(parts[2])
                                .userName(parts[3])
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .toList();

            Set<Long> memberIds = userProjectRepository.findByProjectId(projectId).stream()
                    .map(UserProject::getUserId)
                    .collect(Collectors.toSet());

            return allMatched.stream()
                    .filter(u -> !memberIds.contains(u.getUserId()))
                    .filter(u -> !Objects.equals(u.getUserId(), currentUserId))
                    .toList();
        }
    }