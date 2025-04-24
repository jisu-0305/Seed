package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.project.enums.PlatformType;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PlatformType platformType;

    private String version;
    private String envFileName;
    private String buildTool;

    private Long projectId;
}
