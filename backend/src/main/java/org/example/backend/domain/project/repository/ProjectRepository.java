package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectCustomRepository {
    List<Project> findByIdIn(List<Long> projectIdList);
}
