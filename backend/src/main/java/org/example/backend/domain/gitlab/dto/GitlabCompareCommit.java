package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GitlabCompareCommit {
    private String id;
    private String shortId;
    private String title;
    private String authorName;
    private String authorEmail;
    private OffsetDateTime createdAt;
    private List<String> parentIds;
    private String message;
    private OffsetDateTime authoredDate;
    private String committerName;
    private String committerEmail;
    private OffsetDateTime committedDate;
    private String webUrl;
}
