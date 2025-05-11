package org.example.backend.util.fastai.dto.resolvefile;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResolveRequest {
    private String errorSummary;
    private String cause;
    private String resolutionHint;
    private String filesRaw;
}