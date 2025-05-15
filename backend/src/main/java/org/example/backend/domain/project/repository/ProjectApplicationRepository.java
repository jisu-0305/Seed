package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
    List<ProjectApplication> findAllByProjectId(Long projectId);
    void deleteAllByProjectId(Long projectId);
}
