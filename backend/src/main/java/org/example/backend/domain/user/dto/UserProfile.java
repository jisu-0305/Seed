package org.example.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfile {
    private Long userId;
    private String userName;
    private String userIdentifyId;
    private String profileImageUrl;
    private boolean hasGitlabPersonalAccessToken;
}