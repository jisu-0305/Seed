package org.example.backend.util.aiapi.dto.aireport;

import lombok.Data;

import java.util.List;

@Data
public class AIReportResponse {
    private String title;
    private String summary;
    private List<String> appliedFiles;
    private String additionalNotes;
}