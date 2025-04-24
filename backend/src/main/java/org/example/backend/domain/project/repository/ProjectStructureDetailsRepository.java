package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.ProjectStructureDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectStructureDetailsRepository extends JpaRepository<ProjectStructureDetails, Long> {
    Optional<ProjectStructureDetails> findByProjectId(Long projectId);
}
