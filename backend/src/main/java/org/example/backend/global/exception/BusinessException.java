package org.example.backend.global.exception;

import lombok.Getter;
import org.example.backend.domain.project.enums.ServerStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Long projectId;
    private final ServerStatus serverStatus;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.projectId = null;
        this.serverStatus = null;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.projectId = null;
        this.serverStatus = null;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.projectId = null;
        this.serverStatus = null;
    }

    /**
     * projectId와 serverStatus까지 주면 GlobalExceptionHandler에서 해당 project status 상태값 변경
     * */
    public BusinessException(ErrorCode errorCode, Long projectId, ServerStatus serverStatus) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.projectId = projectId;
        this.serverStatus = serverStatus;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause, Long projectId, ServerStatus serverStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.projectId = projectId;
        this.serverStatus = serverStatus;
    }

    public BusinessException(ErrorCode errorCode, String message, Long projectId, ServerStatus serverStatus) {
        super(message);
        this.errorCode = errorCode;
        this.projectId = projectId;
        this.serverStatus = serverStatus;
    }
}
