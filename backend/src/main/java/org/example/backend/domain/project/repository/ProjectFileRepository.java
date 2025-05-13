package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.ProjectFile;
import org.example.backend.domain.project.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectFileRepository extends JpaRepository <ProjectFile, Long> {
    Optional<ProjectFile> findByProjectIdAndFileType(Long id, FileType fileType);
}
