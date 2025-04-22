package org.example.backend.domain.fcm.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.util.NotificationUtil;
import org.example.backend.domain.fcm.dto.NotificationMessage;
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
    public void notifyUsers(List<Long> userIdList, NotificationMessageTemplate template, String projectName) {
        NotificationMessage message = template.toMessage(projectName);
        notificationUtil.sendToUsers(userIdList, message);
    }


}
