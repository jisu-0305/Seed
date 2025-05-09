package org.example.backend.domain.fcm.enums;

public enum NotificationType {
    INVITATION_CREATED_TYPE,
    INVITATION_ACCEPTED_TYPE,

    /* DB 지운 후 삭제할 것*/
    INVITATION,
    MESSAGE,
}
