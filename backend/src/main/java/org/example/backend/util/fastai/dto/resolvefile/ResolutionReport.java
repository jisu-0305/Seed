package org.example.backend.util.fastai.dto.resolvefile;

import lombok.Data;

@Data
public class ResolutionReport {
    private String errorSummary;
    private String cause;
    private String finalResolution;
}
