package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Long> {
    Optional<ProjectStatus> findByProjectId(Long projectId);
    List<ProjectStatus> findByProjectIdIn(List<Long> projectIdList);
}
