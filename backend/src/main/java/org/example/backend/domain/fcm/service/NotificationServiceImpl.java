package org.example.backend.domain.fcm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.util.NotificationUtil;
import org.example.backend.domain.fcm.dto.NotificationDto;
import org.example.backend.domain.fcm.dto.NotificationMessage;
import org.example.backend.domain.fcm.entity.Notification;
import org.example.backend.domain.fcm.mapper.NotificationMapper;
import org.example.backend.domain.fcm.repository.NotificationRepository;
import org.example.backend.domain.fcm.template.NotificationMessageTemplate;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.domain.userproject.entity.UserProject;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationUtil notificationUtil;
    private final NotificationRepository notificationRepository;
    private final RedisSessionManager redisSessionManager;
    private final UserProjectRepository userProjectRepository;
    private final ProjectRepository projectRepository;

    public void notifyUsers(List<Long> userIdList,
                            NotificationMessageTemplate template,
                            String projectName) {
        // fcm 메시지 전송
        NotificationMessage message = template.toMessage(projectName);
        notificationUtil.sendToUsers(userIdList, message);

        // db에 알림 저장
        List<Notification> notifications = userIdList.stream()
                .map(userId -> Notification.builder()
                        .receiverId(userId)
                        .notificationType(message.getNotificationType())
                        .notificationTitle(message.getNotificationTitle())
                        .notificationContent(message.getNotificationContent())
                        .createdAt(LocalDateTime.now())
                        .isRead(false)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Override
    public void notifyProjectStatusForUsers(Long projectId, NotificationMessageTemplate template) {

        // 프로젝트 이름 찾기
        String projectName = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND))
                .getProjectName();

        // 프로젝트랑 관련된 유저들 찾기
        List<Long> userIds = userProjectRepository.findByProjectId(projectId).stream()
                .map(UserProject::getUserId)
                .collect(Collectors.toList());

        notifyUsers(userIds, template, projectName);
    }

    @Override
    public void notifyInvitationCreated(List<Long> userIdList,
                                        NotificationMessageTemplate template,
                                        String projectName,
                                        Long invitationId) {
        NotificationMessage message = template.toMessage(projectName);
        notificationUtil.sendToUsers(userIdList, message);

        List<Notification> notifications = userIdList.stream()
                .map(userId -> Notification.builder()
                        .receiverId(userId)
                        .notificationType(message.getNotificationType())
                        .notificationTitle(message.getNotificationTitle())
                        .notificationContent(message.getNotificationContent())
                        .createdAt(LocalDateTime.now())
                        .isRead(false)
                        .invitationId(invitationId)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Override
    public void notifyInvitationAccepted(List<Long> userIdList,
                                         NotificationMessageTemplate template,
                                         String projectName,
                                         Long invitationId) {
        NotificationMessage message = template.toMessage(projectName);
        notificationUtil.sendToUsers(userIdList, message);

        List<Notification> notifications = userIdList.stream()
                .map(userId -> Notification.builder()
                        .receiverId(userId)
                        .notificationType(message.getNotificationType())
                        .notificationTitle(message.getNotificationTitle())
                        .notificationContent(message.getNotificationContent())
                        .createdAt(LocalDateTime.now())
                        .isRead(false)
                        .invitationId(invitationId)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Override
    public Page<Notification> getAllNotifications(String accessToken, Pageable pageable) {
        Long userId = redisSessionManager.getSession(accessToken).getUserId();
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public List<NotificationDto> getUnreadNotifications(String accessToken) {
        Long userId = redisSessionManager.getSession(accessToken).getUserId();
        List<Notification> notis = notificationRepository.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return NotificationMapper.toDtoList(notis);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, String accessToken) {
        Long userId = redisSessionManager.getSession(accessToken).getUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!notification.getReceiverId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
        }
    }


}
