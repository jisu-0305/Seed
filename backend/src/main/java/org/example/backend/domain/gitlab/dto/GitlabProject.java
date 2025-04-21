package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabProject {
    private Long id;
    private String name;
    private String pathWithNamespace;
    private String visibility;
    private String httpUrlToRepo;
}
