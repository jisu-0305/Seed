package org.example.backend.domain.docker.enums;

import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;

public enum ContainerActionType {
    RUN, PAUSE, STOP;
    public static ContainerActionType from(String action) {
        String normalized = (action == null || action.isBlank()) ? "RUN" : action.trim().toUpperCase();

        try {
            return ContainerActionType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST, "지원하지 않는 동작: " + action
            );
        }
    }
}
