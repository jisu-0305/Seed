package org.example.backend.domain.fcm.template;

import org.example.backend.domain.fcm.dto.NotificationMessage;
import org.example.backend.domain.fcm.enums.NotificationType;

public enum NotificationMessageTemplate {

    INVITATION_CREATED(
            NotificationType.INVITATION_CREATED_TYPE,
            "초대가 전송되었습니다",
            "\"%s\" 프로젝트에 초대장이 도착했습니다."
    ),
    INVITATION_ACCEPTED(
            NotificationType.INVITATION_ACCEPTED_TYPE,
            "초대가 수락되었습니다",
            "\"%s\" 프로젝트에 새로운 멤버가 합류했습니다."
    );

    private final NotificationType notificationType;
    private final String title;
    private final String bodyFormat;

    NotificationMessageTemplate(NotificationType notificationType,
                                String title,
                                String bodyFormat) {
        this.notificationType = notificationType;
        this.title = title;
        this.bodyFormat = bodyFormat;
    }

    public NotificationMessage toMessage(String projectName) {
        return NotificationMessage.builder()
                .notificationType(this.notificationType)
                .notificationTitle(this.title)
                .notificationContent(String.format(this.bodyFormat, projectName))
                .build();
    }
}
