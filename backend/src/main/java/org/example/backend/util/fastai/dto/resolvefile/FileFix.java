package org.example.backend.util.fastai.dto.resolvefile;

import lombok.Data;

@Data
public class FileFix {
    private String path;
    private String instruction;
    private String explanation;
}
