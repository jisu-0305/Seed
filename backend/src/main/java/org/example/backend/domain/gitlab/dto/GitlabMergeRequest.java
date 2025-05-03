package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitlabMergeRequest {

    private Long iid;

    @JsonProperty("diff_refs")
    private GitlabMergeRequestDiffRefs diffRefs;

}
