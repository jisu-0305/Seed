package org.example.backend.util.aiapi.dto.suspectfile;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SuspectFileRequest {
    private Long projectId;
    private String diffRaw;
    private String tree;
    private String log;
}