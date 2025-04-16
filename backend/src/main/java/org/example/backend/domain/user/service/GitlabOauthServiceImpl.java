package org.example.backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.jwt.JwtTokenProvider;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.domain.user.dto.GitlabOauthToken;
import org.example.backend.domain.user.dto.GitlabUser;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.enums.ProviderType;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitlabOauthServiceImpl implements GitlabOauthService {

    private final WebClient webClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisSessionManager redisSessionManager;

    @Value("${gitlab.application.id}")
    private String applicationId;

    @Value("${gitlab.redirect.uri}")
    private String redirectUri;

    @Value("${gitlab.client.secret}")
    private String clientSecret;

    @Override
    public String buildGitlabAuthorizationUrl() {
        return UriComponentsBuilder.fromUriString("https://lab.ssafy.com/oauth/authorize")
                .queryParam("client_id", applicationId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "read_user")
                .build().toUriString();
    }

    @Override
    public String getAccessToken(String code) {
        GitlabOauthToken oauthToken = getGitlabOauthToken(code);

        if (oauthToken == null) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_FORBIDDEN);
        }

        GitlabUser gitlabUser = getGitlabUser(oauthToken.getAccessToken());

        if (gitlabUser == null) {
            throw new BusinessException(ErrorCode.OAUTH_USER_NOT_FOUND);
        }

        String oauthUserId = gitlabUser.getId();

        User user = userRepository.findByOauthUserId(oauthUserId)
                .map(existingUser -> {
                    existingUser.setAccessToken(oauthToken.getAccessToken());
                    existingUser.setRefreshToken(oauthToken.getRefreshToken());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .accessToken(oauthToken.getAccessToken())
                            .refreshToken(oauthToken.getRefreshToken())
                            .providerType(ProviderType.GITLAB)
                            .oauthUserId(oauthUserId)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });

        String jwtToken = jwtTokenProvider.generateToken(user, oauthUserId);
        redisSessionManager.saveSession(jwtToken, user, oauthUserId);
        return jwtToken;
    }

    @Override
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.INVALID_AUTHORIZATION_HEADER);
        }
        String jwtToken = authorizationHeader.substring(7);
        redisSessionManager.deleteSession(jwtToken);
    }

    private GitlabOauthToken getGitlabOauthToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", applicationId);
        formData.add("client_secret", clientSecret);
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", redirectUri);

        return webClient.post()
                .uri("https://lab.ssafy.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(GitlabOauthToken.class)
                .block();
    }

    private GitlabUser getGitlabUser(String accessToken) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("lab.ssafy.com")
                        .path("/api/v4/user")
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .bodyToMono(GitlabUser.class)
                .block();
    }

}
