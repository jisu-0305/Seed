package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p.projectName FROM Project p WHERE p.id = :projectId")
    String findProjectNameById(@Param("projectId") Long projectId);
}
