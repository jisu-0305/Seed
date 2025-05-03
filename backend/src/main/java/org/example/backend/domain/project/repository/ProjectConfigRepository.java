package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.ProjectConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectConfigRepository extends JpaRepository<ProjectConfig, Long> {
    Optional<ProjectConfig> findByProjectId(Long projectId);
}
