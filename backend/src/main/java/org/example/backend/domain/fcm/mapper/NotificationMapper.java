package org.example.backend.domain.fcm.mapper;

import org.example.backend.domain.fcm.dto.NotificationDto;
import org.example.backend.domain.fcm.entity.Notification;
import org.springframework.data.domain.Page;

import java.util.List;

public class NotificationMapper {

    public static NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .notificationTitle(notification.getNotificationTile())
                .notificationContent(notification.getNotificationContent())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public static List<NotificationDto> toDtoList(List<Notification> notifications) {
        return notifications.stream().map(NotificationMapper::toDto).toList();
    }

    public static Page<NotificationDto> toDtoPage(Page<Notification> page) {
        return page.map(NotificationMapper::toDto);
    }
}