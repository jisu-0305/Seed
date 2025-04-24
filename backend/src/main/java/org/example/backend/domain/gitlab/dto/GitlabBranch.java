package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabBranch {

    private GitlabCommit commit;
    private String name;
    private boolean merged;

    @JsonProperty("protected")
    private boolean protectedBranch;

    @JsonProperty("default")
    private boolean defaultBranch;

    @JsonProperty("developers_can_push")
    private boolean developersCanPush;

    @JsonProperty("developers_can_merge")
    private boolean developersCanMerge;

    @JsonProperty("can_push")
    private boolean canPush;

    @JsonProperty("web_url")
    private String webUrl;

}
