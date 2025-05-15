package org.example.backend.domain.jenkins.enums;

public enum BuildStatusType {
    SUCCESS, FAIL,  UNSTABLE, ABORTED, NOT_BUILT, UNKNOWN;  // 빈 문자열 또는 null 을 매핑

    public static BuildStatusType from(String raw) {
        if (raw == null || raw.isEmpty()) {
            return UNKNOWN;
        }
        switch (raw.toUpperCase()) {
            case "SUCCESS":
                return SUCCESS;
            case "FAILURE":
            case "FAILED":
                return FAIL;
            case "UNSTABLE":
                return UNSTABLE;
            case "ABORTED":
                return ABORTED;
            case "NOT_BUILT":
                return NOT_BUILT;
            default:
                return UNKNOWN;
        }
    }
}
