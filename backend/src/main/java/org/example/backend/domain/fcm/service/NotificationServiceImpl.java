package org.example.backend.domain.fcm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.util.NotificationUtil;
import org.example.backend.domain.fcm.dto.NotificationMessage;
import org.example.backend.domain.fcm.entity.Notification;
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
                        .title(message.getTitle())
                        .body(message.getBody())
                        .sentAt(LocalDateTime.now())
                        .isRead(false)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Override
    public Page<Notification> getAllNotifications(String accessToken, Pageable pageable) {
        Long userId = redisSessionManager.getSession(accessToken).getUserId();
        return notificationRepository.findByReceiverIdOrderBySentAtDesc(userId, pageable);
    }

    @Override
    public List<Notification> getUnreadNotifications(String accessToken) {
        Long userId = redisSessionManager.getSession(accessToken).getUserId();
        return notificationRepository.findByReceiverIdAndIsReadFalseOrderBySentAtDesc(userId);
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
