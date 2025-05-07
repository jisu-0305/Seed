package org.example.backend.util.fastai.dto;

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
