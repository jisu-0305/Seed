package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitlabMergeRequestDiffRefs {

    @JsonProperty("base_sha")
    private String baseSha;

    @JsonProperty("head_sha")
    private String headSha;

    @JsonProperty("start_sha")
    private String startSha;

}
