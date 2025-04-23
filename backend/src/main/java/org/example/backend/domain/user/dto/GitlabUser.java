package org.example.backend.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitlabUser {
    private String id;
    private String name;
    private String username;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
