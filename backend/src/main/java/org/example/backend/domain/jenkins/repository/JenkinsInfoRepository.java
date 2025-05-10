package org.example.backend.domain.jenkins.repository;

import org.example.backend.domain.jenkins.entity.JenkinsInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JenkinsInfoRepository extends JpaRepository<JenkinsInfo, Long> {
    Optional<JenkinsInfo> findByProjectId(Long projectId);
}