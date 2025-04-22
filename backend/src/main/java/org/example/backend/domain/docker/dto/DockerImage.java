package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerImage {
    @JsonProperty("repo_name")
    private String repoName;

    @JsonProperty("short_description")
    private String shortDescription;

    @JsonProperty("star_count")
    private long starCount;

    @JsonProperty("pull_count")
    private long pullCount;

    @JsonProperty("repo_owner")
    private String repoOwner;

    @JsonProperty("is_automated")
    private boolean isAutomated;

    @JsonProperty("is_official")
    private boolean isOfficial;
}
