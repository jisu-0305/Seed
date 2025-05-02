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

    private String projectName;
    private ExecutionType type;
    private String title;
    private BuildStatus status;
    private String buildNumber;
    private LocalDate executionDate;
    private LocalTime executionTime;
}
