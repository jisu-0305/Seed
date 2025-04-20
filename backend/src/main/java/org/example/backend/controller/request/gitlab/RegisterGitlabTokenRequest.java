package org.example.backend.controller.request.gitlab;

import jakarta.validation.constraints.NotBlank;

public record RegisterGitlabTokenRequest(
        @NotBlank(message = "token must not be blank")
        String token
) {
}
