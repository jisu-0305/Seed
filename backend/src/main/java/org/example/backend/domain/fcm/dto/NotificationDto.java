package org.example.backend.domain.fcm.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationDto {
    private Long id;
    private String title;
    private String body;
    private boolean isRead;
    private LocalDateTime sentAt;
}
