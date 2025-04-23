package org.example.backend.domain.user.service;

import org.example.backend.domain.user.dto.UserProfile;

public interface GitlabOauthService {
    String buildGitlabAuthorizationUrl();

    String getAccessToken(String code);

    void logout(String authorizationHeader);

    boolean login(String accessToken);

    UserProfile getUserProfile(String jwtToken);
}
