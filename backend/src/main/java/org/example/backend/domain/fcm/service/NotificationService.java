package org.example.backend.domain.fcm.service;

import org.example.backend.domain.fcm.dto.NotificationDto;
import org.example.backend.domain.fcm.entity.Notification;
import org.example.backend.domain.fcm.template.NotificationMessageTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
//    void notifyUsers(List<Long> userIdList, NotificationMessageTemplate template, String projectName);
    void notifyProjectStatusForUsers(Long projectId, NotificationMessageTemplate template);
    void notifyInvitationCreated(List<Long> userIdList,
                                 NotificationMessageTemplate template,
                                 String projectName,
                                 Long invitationId);
    void notifyInvitationAccepted(List<Long> userIdList,
                                  NotificationMessageTemplate template,
                                  String projectName,
                                  Long invitationId);
    Page<Notification> getAllNotifications(String accessToken, Pageable pageable);
    List<NotificationDto> getUnreadNotifications(String accessToken);
    void markAsRead(Long notificationId, String accessToken);
}
