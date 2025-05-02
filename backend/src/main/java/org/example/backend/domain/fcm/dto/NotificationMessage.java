package org.example.backend.domain.fcm.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationMessage {
    private final String notificationTitle;
    private final String notificationContent;
}
