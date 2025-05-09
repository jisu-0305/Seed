package org.example.backend.domain.server.repository;

import org.example.backend.domain.server.entity.HttpsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HttpsLogRepository extends JpaRepository<HttpsLog, Long> {
    List<HttpsLog> findByProjectIdOrderByCreatedAtAsc(Long projectId);
    List<HttpsLog> findAllByProjectIdOrderByCreatedAtAsc(Long projectId);
}
