package org.example.backend.domain.gitlab.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitlabFileContentDto {
    private String fileName;
    private String path;
    private String ref;
    private String encoding;
    private Long size;
    private String content;
}
