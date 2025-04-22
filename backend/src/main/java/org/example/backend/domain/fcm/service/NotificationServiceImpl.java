package org.example.backend.domain.fcm.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.util.NotificationUtil;
import org.example.backend.domain.fcm.dto.NotificationMessage;
import org.example.backend.domain.fcm.entity.NotificationType;
import org.example.backend.domain.fcm.template.NotificationMessageTemplate;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationUtil notificationUtil;

    @Override
    public void notifyUsers(List<Long> userIdList, NotificationType type, String projectName) {
        NotificationMessageTemplate template = convertToTemplate(type);
        NotificationMessage message = template.toMessage(projectName);
        notificationUtil.sendToUsers(userIdList, message);
    }

    private NotificationMessageTemplate convertToTemplate(NotificationType type) {
        return switch (type) {
            case INVITATION_ACCEPTED -> NotificationMessageTemplate.INVITATION_ACCEPTED;
            case INVITATION_CREATED -> NotificationMessageTemplate.INVITATION_CREATED;
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_NOTIFICATION_TYPE);
        };
    }
}
