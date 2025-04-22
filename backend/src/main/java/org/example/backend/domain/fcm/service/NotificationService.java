package org.example.backend.domain.fcm.service;

import org.example.backend.domain.fcm.entity.NotificationType;

import java.util.List;

public interface NotificationService {
    void notifyUsers(List<Long> userIdList, NotificationType type, String projectName);
}