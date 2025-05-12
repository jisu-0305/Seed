package org.example.backend.util.aiapi.dto.aireport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.util.aiapi.dto.resolvefile.FileFix;
import org.example.backend.util.aiapi.dto.resolvefile.ResolutionReport;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIReportRequest {
    private List<FileFix> fileFixes;
    private ResolutionReport resolutionReport;
}