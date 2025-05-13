package org.example.backend.domain.aireport.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "applied_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppliedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private Long aiReportId;

    public AppliedFile(String fileName, Long aiReportId) {
        this.fileName = fileName;
        this.aiReportId = aiReportId;
    }
} 
