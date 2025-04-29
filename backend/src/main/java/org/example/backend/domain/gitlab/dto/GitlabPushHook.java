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
public class GitlabPushHook {
    private String objectKind;
    private String eventName;
    private String before; // 푸시 이전에 해당 브랜치나 태그가 가리키고 있던 마지막 커밋의 sha 해시값
    private String after; // 푸시 이후에 해당 브랜치나 태그가 가리키게 된 최신 커밋의 sha 해시값
    private String ref;
    private Boolean refProtected;
    private String checkoutSha;

    private Long userId;
    private String userName;
    private String userUsername;
    private String userEmail;
    private String userAvatar;

    private Long projectId;
    private GitlabPushHookProject project;
    private GitlabPushHookRepository repository;

    private List<GitlabPushHookCommit> commits;
    private Integer totalCommitsCount;
}
