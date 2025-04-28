package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GitlabPushHookRepository {
    private String name;
    private String url;
    private String description;
    private String homepage;
    private String gitHttpUrl;
    private String gitSshUrl;
    private Integer visibilityLevel;
}
