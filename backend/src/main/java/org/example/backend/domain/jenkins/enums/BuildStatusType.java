package org.example.backend.domain.jenkins.enums;

public enum BuildStatusType {
    SUCCESS, FAIL,  UNSTABLE, ABORTED, NOT_BUILT, UNKNOWN;  // 빈 문자열 또는 null 을 매핑

    public static BuildStatusType from(String raw) {
        if (raw == null || raw.isEmpty()) {
            return UNKNOWN;
        }
        return switch (raw.toUpperCase()) {
            case "SUCCESS" -> SUCCESS;
            case "FAILURE", "FAILED" -> FAIL;
            case "UNSTABLE" -> UNSTABLE;
            case "ABORTED" -> ABORTED;
            case "NOT_BUILT" -> NOT_BUILT;
            default -> UNKNOWN;
        };
    }
}
