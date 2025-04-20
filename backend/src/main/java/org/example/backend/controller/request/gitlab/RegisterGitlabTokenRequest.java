package org.example.backend.controller.request.gitlab;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterGitlabTokenRequest {

    @NotBlank(message = "token must not be blank")
    private String token;
}