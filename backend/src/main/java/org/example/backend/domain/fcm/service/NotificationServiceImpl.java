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
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationUtil notificationUtil;
    private final NotificationRepository notificationRepository;
    private final RedisSessionManager redisSessionManager;


    @Override
    public void notifyUsers(List<Long> userIdList, NotificationMessageTemplate template, String projectName) {
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
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
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

//    @Override
//    public List<Notification> getUnreadNotifications(String accessToken) {
//        Long userId = redisSessionManager.getSession(accessToken).getUserId();
//        return notificationRepository.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId);
//    }
//
@Override
    public List<NotificationDto> getUnreadNotifications(String accessToken) {
        Long userId = redisSessionManager.getSession(accessToken).getUserId();

        // 1) Notification 엔티티 리스트 조회
        List<Notification> notis = notificationRepository
                .findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        // 2) 엔티티 → DTO 로 변환 (invitationId 포함!)
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
