package org.example.backend.controller.response.log;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HttpsLogResponse {
    private int stepNumber;
    private String stepName;
    private String logContent;
    private String status;
    private LocalDateTime createdAt;
}
