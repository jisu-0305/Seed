package org.example.backend.domain.user.service;

import org.example.backend.domain.user.dto.AuthResponse;
import org.example.backend.domain.user.dto.UserProfile;

public interface GitlabOauthService {
    String buildGitlabAuthorizationUrl();

    AuthResponse processUserAndSave(String code);

    void logout(String authorizationHeader);

    boolean login(String accessToken);

    UserProfile getUserProfile(String jwtToken);

    void updatePersonalAccessToken(String accessToken, String pat);
}
