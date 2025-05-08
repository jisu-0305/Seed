package org.example.backend.domain.fcm.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.domain.fcm.enums.NotificationType;

@Getter
@Builder
public class NotificationMessage {
    private final String notificationTitle;
    private final String notificationContent;
    private final NotificationType notificationType;
}
