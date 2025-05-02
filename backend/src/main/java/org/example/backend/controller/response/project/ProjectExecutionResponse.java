package org.example.backend.controller.response.project;

import lombok.*;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.project.enums.ExecutionType;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectExecutionResponse {

    private Long id;
    private String projectName;
    private ExecutionType executionType;
    private String executionTitle;
    private BuildStatus executionStatus;
    private String buildNumber;
    private LocalDate createdAt;
}
