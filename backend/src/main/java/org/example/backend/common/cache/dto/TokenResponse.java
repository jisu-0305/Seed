package org.example.backend.common.cache.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("expires_in")
    private long expiresIn;
}
