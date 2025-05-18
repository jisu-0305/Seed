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
    ),
    EC2_SETUP_COMPLETED_SUCCESS(
            NotificationType.EC2_SETUP_COMPLETED_TYPE,
            "EC2 서버 세팅 완료",
            "\"%s\" EC2 인스턴스 설정이 성공적으로 완료되었습니다."
    ),
    EC2_SETUP_FAILED(
            NotificationType.EC2_SETUP_FAILED_TYPE,
            "EC2 서버 세팅 실패",
            "\"%s\" EC2 인스턴스 설정에 실패했습니다. 설정을 다시 시도해 주세요."
    ),
    HTTPS_SETUP_COMPLETED(
            NotificationType.HTTPS_SETUP_COMPLETED_TYPE,
            "HTTPS 설정 완료",
            "\"%s\" 도메인에 대한 HTTPS 인증서 적용이 완료되었습니다."
    ),
    HTTPS_SETUP_FAILED(
            NotificationType.HTTPS_SETUP_FAILED_TYPE,
            "HTTPS 설정 실패",
            "\"%s\" 도메인에 대한 HTTPS 인증서 적용에 실패했습니다. 인증서 설정을 검토해 주세요."
    ),
    CICD_BUILD_COMPLETED(
            NotificationType.CICD_BUILD_COMPLETED_TYPE,
            "CI/CD 빌드 완료",
            "\"%s\" 프로젝트의 CI/CD 빌드가 성공적으로 완료되었습니다."
    ),
    CICD_BUILD_FAILED(
            NotificationType.CICD_BUILD_FAILED_TYPE,
            "CI/CD 빌드 실패",
            "\"%s\" 프로젝트의 CI/CD 빌드가 실패했습니다. 빌드 로그를 확인해주세요."
    ),
    AI_REPORT_CREATED(
            NotificationType.AI_REPORT_CREATED_TYPE,
            "새 AI 보고서 도착",
            "\"%s\" 이름의 새로운 AI 보고서가 생성되었습니다."
    ),
    AI_REPORT_CREATED_FAILED(
            NotificationType.AI_REPORT_CREATED_FAILED_TYPE,
            "AI 보고서 생성 실패",
            "\"%s\" AI 보고서 생성에 실패했습니다. 다시 시도해주세요."
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
