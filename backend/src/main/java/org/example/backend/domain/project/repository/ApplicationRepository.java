package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findAllByProjectId(Long projectId);
}
