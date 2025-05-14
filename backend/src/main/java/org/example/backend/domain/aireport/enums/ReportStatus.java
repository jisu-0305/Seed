package org.example.backend.domain.aireport.enums;

public enum ReportStatus {
    SUCCESS,
    FAIL;

    public static ReportStatus fromJenkinsStatus(String jenkinsStatus) {
        if ("SUCCESS".equalsIgnoreCase(jenkinsStatus)) {
            return SUCCESS;
        } else {
            return FAIL; // FAILURE, ABORTED
        }
    }
}
