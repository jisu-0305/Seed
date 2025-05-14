package org.example.backend.util.aiapi.dto.resolvefile;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileInnerResponse;

@Getter
@Setter
@Builder
public class ResolveRequest {
    private SuspectFileInnerResponse suspectFileInnerResponse;
    private String filesRaw;
}