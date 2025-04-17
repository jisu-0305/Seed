package org.example.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.error("BusinessException 발생 : code={} message={}", errorCode.getCode(), ex.getMessage());

        return new ResponseEntity<>(ErrorResponse.error(errorCode), errorCode.getStatus());
    }
}
