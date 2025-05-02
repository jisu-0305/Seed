package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.project.enums.ExecutionType;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "project_execution")
public class ProjectExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    @Enumerated(EnumType.STRING)
    private ExecutionType type;

    private String title;

    @Enumerated(EnumType.STRING)
    private BuildStatus status;

    private String buildNumber;

    private LocalDate executionDate;
    private LocalTime executionTime;
}

