package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.ProjectExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectExecutionRepository extends JpaRepository<ProjectExecution, Long> {
    List<ProjectExecution> findByProjectIdInOrderByCreatedAtDesc(List<Long> projectIds);

}
