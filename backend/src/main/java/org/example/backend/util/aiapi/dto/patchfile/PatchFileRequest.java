package org.example.backend.util.aiapi.dto.patchfile;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatchFileRequest {
    private String path;
    private String originalCode;
    private String instruction;
}
