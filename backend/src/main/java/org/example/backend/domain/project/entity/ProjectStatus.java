package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.project.enums.BuildStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "project_status")
public class ProjectStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    @Enumerated(EnumType.STRING)
    private BuildStatus lastBuildStatus;

    private LocalDateTime lastBuildAt;

    private boolean httpsEnabled;
    private boolean autoDeployEnabled;

    public void enableHttps() {
        this.httpsEnabled = true;
    }

    public void updateBuildStatus(BuildStatus status) {
        this.lastBuildStatus = status;
        this.lastBuildAt = LocalDateTime.now();
    }
}