package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GitlabPushHookCommit {
    private String id;
    private String message;
    private String title;
    private String timestamp;
    private String url;
    private GitlabAuthor author;
    private List<String> added;
    private List<String> modified;
    private List<String> removed;
}
