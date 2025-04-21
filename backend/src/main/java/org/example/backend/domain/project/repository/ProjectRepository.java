package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
