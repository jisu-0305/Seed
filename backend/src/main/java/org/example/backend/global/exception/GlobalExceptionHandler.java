package org.example.backend.global.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ProjectRepository projectRepository;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        if (ex.getProjectId() != null && ex.getServerStatus() != null) {
            projectRepository.findById(ex.getProjectId()).ifPresent(project -> {
                project.updateAutoDeploymentStatus(ex.getServerStatus());
            });
        }
        ErrorCode errorCode = ex.getErrorCode();

        return new ResponseEntity<>(ErrorResponse.error(errorCode), errorCode.getStatus());
    }
}
