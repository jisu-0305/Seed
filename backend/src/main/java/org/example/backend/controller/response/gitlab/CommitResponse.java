package org.example.backend.controller.response.gitlab;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommitResponse {
    @JsonProperty("web_url")
    private String webUrl;
}
