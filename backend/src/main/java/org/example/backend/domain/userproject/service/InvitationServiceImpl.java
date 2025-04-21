    package org.example.backend.domain.userproject.service;

    import lombok.RequiredArgsConstructor;
    import org.example.backend.common.session.RedisSessionManager;
    import org.example.backend.common.session.dto.SessionInfoDto;
    import org.example.backend.controller.request.userProject.InvitationRequest;
    import org.example.backend.controller.response.userproject.InvitationResponse;
    import org.example.backend.domain.userproject.entity.Invitation;
    import org.example.backend.domain.userproject.entity.UserProject;
    import org.example.backend.domain.userproject.mapper.InvitationMapper;
    import org.example.backend.domain.userproject.repository.InvitationRepository;
    import org.example.backend.domain.userproject.repository.UserProjectRepository;
    import org.example.backend.global.exception.BusinessException;
    import org.example.backend.global.exception.ErrorCode;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;

    import static org.example.backend.domain.userproject.mapper.InvitationMapper.toResponse;

    @Service
    @RequiredArgsConstructor
    public class InvitationServiceImpl implements InvitationService {

        private final InvitationRepository invitationRepository;
        private final UserProjectRepository userProjectRepository;
        private final RedisSessionManager redisSessionManager;

        private Long getUserIdFromToken(String rawToken) {
            String jwtToken = rawToken.replace("Bearer", "").trim();
            SessionInfoDto session = redisSessionManager.getSession(jwtToken);
            if (session == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
            return session.getUserId();
        }

        @Override
        @Transactional
        public InvitationResponse sendInvitation(InvitationRequest request, String accessToken) {
            Long senderId = getUserIdFromToken(accessToken);

            if (senderId.equals(request.getReceiverId())) {
                throw new BusinessException(ErrorCode.CANNOT_INVITE_SELF);
            }

            boolean isSenderMember = userProjectRepository.existsByProjectIdAndUserId(request.getProjectId(), senderId);
            if (!isSenderMember) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            boolean alreadyInvited = invitationRepository.existsByProjectIdAndReceiverId(
                    request.getProjectId(), request.getReceiverId()
            );
            if (alreadyInvited) {
                throw new BusinessException(ErrorCode.DUPLICATE_INVITATION);
            }

            Invitation invitation = Invitation.create(
                    request.getProjectId(), senderId, request.getReceiverId()
            );

            Invitation saved = invitationRepository.save(invitation);
            return toResponse(saved);
        }

        @Override
        @Transactional
        public void acceptInvitation(Long invitationId, String accessToken) {
            Long userId = getUserIdFromToken(accessToken);

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
            invitationRepository.delete(invitation);
        }

        @Override
        @Transactional
        public void rejectInvitation(Long invitationId, String accessToken) {
            Long userId = getUserIdFromToken(accessToken);

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
            Long receiverId = getUserIdFromToken(accessToken);
            return invitationRepository.findByReceiverId(receiverId).stream()
                    .map(InvitationMapper::toResponse)
                    .toList();
        }
    }