package org.example.backend.util.fastai.dto.resolvefile;

import lombok.Data;

import java.util.List;

@Data
public class ResolveErrorInnerResponse {
    private List<FileFix> fileFixes;
    private ResolutionReport resolutionReport;
}
