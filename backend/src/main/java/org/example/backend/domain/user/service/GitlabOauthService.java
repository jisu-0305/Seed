package org.example.backend.domain.user.service;

public interface GitlabOauthService {
    String buildGitlabAuthorizationUrl();

    String getAccessToken(String code);

    void logout(String authorizationHeader);

    boolean login(String accessToken);
}
