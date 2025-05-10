package org.example.backend.domain.fcm.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.fcm.enums.NotificationType;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationDto {
    private Long id;
    private String notificationTitle;
    private String notificationContent;
    private boolean isRead;
    private LocalDateTime createdAt;
    private NotificationType notificationType;
}
