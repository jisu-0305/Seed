package org.example.backend.domain.fcm.service;

import org.example.backend.domain.fcm.template.NotificationMessageTemplate;

import java.util.List;

public interface NotificationService {
    void notifyUsers(List<Long> userIdList, NotificationMessageTemplate template, String projectName);
}