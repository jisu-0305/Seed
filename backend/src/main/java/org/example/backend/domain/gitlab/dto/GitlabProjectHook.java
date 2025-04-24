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
public class GitlabProjectHook {
    private String objectKind;
    private String eventName;
    private String before;
    private String after;
    private String ref;
    private Boolean refProtected;
    private String checkoutSha;

    private Long userId;
    private String userName;
    private String userUsername;
    private String userEmail;
    private String userAvatar;

    private Long projectId;
    private Project project;
    private Repository repository;

    private List<Commit> commits;
    private Integer totalCommitsCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Project {
        private Long id;
        private String name;
        private String description;
        private String webUrl;
        private String avatarUrl;
        private String gitSshUrl;
        private String gitHttpUrl;
        private String namespace;
        private Integer visibilityLevel;
        private String pathWithNamespace;
        private String defaultBranch;
        private String homepage;
        private String url;
        private String sshUrl;
        private String httpUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Repository {
        private String name;
        private String url;
        private String description;
        private String homepage;
        private String gitHttpUrl;
        private String gitSshUrl;
        private Integer visibilityLevel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Commit {
        private String id;
        private String message;
        private String title;
        private String timestamp;
        private String url;
        private Author author;
        private List<String> added;
        private List<String> modified;
        private List<String> removed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Author {
        private String name;
        private String email;
    }
}

