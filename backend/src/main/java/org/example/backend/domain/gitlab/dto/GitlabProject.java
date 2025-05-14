package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabProject {

    @JsonProperty("id")
    private Long gitlabProjectId;

    private String name;

    @JsonProperty("path_with_namespace")
    private String pathWithNamespace;

    private String visibility;

    @JsonProperty("http_url_to_repo")
    private String httpUrlToRepo;

    @JsonProperty("default_branch")
    private String defaultBranch;

}
