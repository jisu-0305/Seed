package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommitAction {

    private String action;

    @JsonProperty("file_path")
    private String filePath;

    private String content;
}
