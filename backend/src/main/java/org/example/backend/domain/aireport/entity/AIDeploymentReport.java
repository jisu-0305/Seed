package org.example.backend.domain.aireport.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.aireport.enums.ReportStatus;
import org.example.backend.util.aiapi.dto.aireport.ReportResponse;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ai_deployment_reports")
public class AIDeploymentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    private String commitUrl;

    private String mergeRequestUrl;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private LocalDateTime createdAt;

    public static AIDeploymentReport of(Long projectId, String title, ReportResponse response, String commitUrl, String mergeRequestUrl, ReportStatus status) {
        return AIDeploymentReport.builder()
                .projectId(projectId)
                .title(title)
                .summary(response.getSummary())
                .additionalNotes(response.getAdditionalNotes())
                .commitUrl(commitUrl)
                .mergeRequestUrl(mergeRequestUrl)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }
}