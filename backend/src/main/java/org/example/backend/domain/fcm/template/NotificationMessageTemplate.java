package org.example.backend.domain.fcm.template;

import org.example.backend.domain.fcm.dto.NotificationMessage;

public enum NotificationMessageTemplate {

    INVITATION_CREATED("초대가 전송되었습니다", "\"%s\" 프로젝트에 초대장이 도착했습니다."),
    INVITATION_ACCEPTED("초대가 수락되었습니다", "\"%s\" 프로젝트에 새로운 멤버가 합류했습니다.");


    private final String title;
    private final String bodyFormat;

    NotificationMessageTemplate(String title, String bodyFormat) {
        this.title = title;
        this.bodyFormat = bodyFormat;
    }

    public NotificationMessage toMessage(String projectName) {
        return NotificationMessage.builder()
                .notificationTitle(this.title)
                .notificationContent(String.format(this.bodyFormat, projectName))
                .build();
    }
}
