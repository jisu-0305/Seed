package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.project.enums.ProjectStructure;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;
    private String projectName;
    private String serverIP;
    private LocalDateTime createdAt;
    private String repositoryUrl;

    @Enumerated(EnumType.STRING)
    private ProjectStructure structure;

    private String frontendBranchName;
    private String frontendDirectoryName;
    private String backendBranchName;
    private String backendDirectoryName;
    private String pemFilePath;
}

