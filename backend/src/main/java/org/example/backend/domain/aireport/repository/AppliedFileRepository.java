package org.example.backend.domain.aireport.repository;

import org.example.backend.domain.aireport.entity.AppliedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppliedFileRepository extends JpaRepository<AppliedFile, Long> {
    List<AppliedFile> findAllByReportId(Long reportId);
}
