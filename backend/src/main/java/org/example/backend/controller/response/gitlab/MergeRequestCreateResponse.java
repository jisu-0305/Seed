package org.example.backend.controller.response.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MergeRequestCreateResponse {
    private Long id;
    private Integer iid;
    private Long projectId;
    private String title;
    private String description;
    private String state;
    private String sourceBranch;
    private String targetBranch;
    private String webUrl;
    private String createdAt;
}
