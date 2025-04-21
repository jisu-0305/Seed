package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;

    private LocalDateTime createdAt;

    public static Project create(String projectName) {
        return Project.builder()
                .projectName(projectName)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
