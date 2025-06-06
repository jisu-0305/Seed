package org.example.backend.domain.server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "https_logs")
public class HttpsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private String stepName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String logContent;

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;
}

