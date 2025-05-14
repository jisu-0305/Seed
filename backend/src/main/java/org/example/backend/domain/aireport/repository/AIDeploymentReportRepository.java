package org.example.backend.domain.aireport.repository;

import org.example.backend.domain.aireport.entity.AIDeploymentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIDeploymentReportRepository extends JpaRepository<AIDeploymentReport, Long> {
    List<AIDeploymentReport> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);
}
