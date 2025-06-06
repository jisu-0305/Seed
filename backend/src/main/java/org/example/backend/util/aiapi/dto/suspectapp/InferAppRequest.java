package org.example.backend.util.aiapi.dto.suspectapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.backend.domain.gitlab.dto.GitlabCompareDiff;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferAppRequest {

    @JsonProperty("projectId")
    private Long projectId;

    @JsonProperty("gitDiff")
    private List<GitlabCompareDiff> gitDiff;

    @JsonProperty("jenkinsLog")
    private String jenkinsLog;

    @JsonProperty("applicationNames")
    private List<String> applicationNames;
}