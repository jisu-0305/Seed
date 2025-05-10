package org.example.backend.util.fastai.dto.suspectfile;

import lombok.Data;

import java.util.List;

@Data
public class SuspectFileInnerResponse {
    private String errorSummary;
    private String cause;
    private String resolutionHint;
    private List<SuspectFilePath> suspectFiles;
}

