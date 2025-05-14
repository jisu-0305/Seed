package org.example.backend.util.aiapi.dto.patchfile;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatchTextRequest {
    private String originalCode;
    private String instruction;
}
