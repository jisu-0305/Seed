package org.example.backend.util.fastai.dto.aireport;

import lombok.Data;

import java.util.List;

@Data
public class ReportResponseDto {
    private String summary;
    private List<String> appliedFiles;
    private String additionalNotes;
}