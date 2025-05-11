package org.example.backend.util.fastai.dto.resolvefile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileFix {
    private String path;
    private String instruction;
    private String explanation;
}
