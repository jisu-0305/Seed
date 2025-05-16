package org.example.backend.domain.fcm.enums;

public enum NotificationType {
    // 초대 관련
    INVITATION_CREATED_TYPE,
    INVITATION_ACCEPTED_TYPE,

    // EC2 세팅
    EC2_SETUP_COMPLETED_TYPE,     // EC2 세팅 완료
    EC2_SETUP_FAILED_TYPE,

    // HTTPS
    HTTPS_SETUP_COMPLETED_TYPE,   // HTTPS 세팅 완료
    HTTPS_SETUP_FAILED_TYPE,

    // CI/CD 빌드
    CICD_BUILD_COMPLETED_TYPE,    // CI/CD 빌드 완료
    CICD_BUILD_FAILED_TYPE,

    // AI 보고서
    AI_REPORT_CREATED_TYPE,        // 새로운 AI 보고서 생성
    AI_REPORT_CREATED_FAILED_TYPE,

    /* DB 지운 후 삭제할 것*/
    INVITATION,
    MESSAGE,
}
