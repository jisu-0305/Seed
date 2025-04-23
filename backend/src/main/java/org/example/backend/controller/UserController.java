package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.user.dto.UserProfile;
import org.example.backend.domain.user.service.GitlabOauthService;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final GitlabOauthService gitlabOauthService;

    @GetMapping("/oauth/gitlab/login")
    public ResponseEntity<ApiResponse<Void>> gitlabLogin(
            HttpServletResponse httpServletResponse,
            @RequestHeader(value = "Authorization", required = false) String accessToken) throws IOException {

        boolean isLogined = gitlabOauthService.login(accessToken);

        if (!isLogined) {
            String authorizationUrl = gitlabOauthService.buildGitlabAuthorizationUrl();
            httpServletResponse.sendRedirect(authorizationUrl);
        }

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/oauth/gitlab/callback")
    public ResponseEntity<ApiResponse<Void>> gitlabCallback(@RequestParam("code") String code) {
        String accessToken = gitlabOauthService.getAccessToken(code);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(ApiResponse.success());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader) {

        gitlabOauthService.logout(authorizationHeader);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/me")
    @Operation(summary = "로그인한 사용자 정보 조회", security = @SecurityRequirement(name = "JWT") )
    public ResponseEntity<ApiResponse<UserProfile>> getUserProfile(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {

        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.INVALID_AUTHORIZATION_HEADER);
        }

        UserProfile profile = gitlabOauthService.getUserProfile(accessToken);

        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
