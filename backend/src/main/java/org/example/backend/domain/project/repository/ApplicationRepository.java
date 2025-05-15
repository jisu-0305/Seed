package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByImageName(String imageName);
    List<Application> findByImageNameContainingIgnoreCase(String keyword);
    boolean existsByImageNameAndDefaultPort(String imageName, int defaultPort);
}
