package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabCommit {

    private String id;

    @JsonProperty("short_id")
    private String shortId;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("parent_ids")
    private List<String> parentIds;

    private String title;
    private String message;

    @JsonProperty("author_name")
    private String authorName;

    @JsonProperty("author_email")
    private String authorEmail;

    @JsonProperty("authored_date")
    private OffsetDateTime authoredDate;

    @JsonProperty("committer_name")
    private String committerName;

    @JsonProperty("committer_email")
    private String committerEmail;

    @JsonProperty("committed_date")
    private OffsetDateTime committedDate;

    private Map<String, String> trailers;

    @JsonProperty("web_url")
    private String webUrl;

}
