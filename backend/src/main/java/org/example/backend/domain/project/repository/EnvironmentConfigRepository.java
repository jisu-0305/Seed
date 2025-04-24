package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.EnvironmentConfig;
import org.example.backend.domain.project.enums.PlatformType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnvironmentConfigRepository extends JpaRepository<EnvironmentConfig, Long> {
    Optional<EnvironmentConfig> findByProjectIdAndPlatformType(Long projectId, PlatformType platformType);
}
