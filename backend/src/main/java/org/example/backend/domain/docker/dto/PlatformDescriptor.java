package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown=true)
public class PlatformDescriptor {
    private String digest;
    private Platform platform;
    @Data
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Platform {
        private String architecture;
        private String os;
    }
}

