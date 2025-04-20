package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabProjectDto {
    private Long   id;
    private String name;
    private String path_with_namespace;
    private String visibility;
    private String http_url_to_repo;
}
