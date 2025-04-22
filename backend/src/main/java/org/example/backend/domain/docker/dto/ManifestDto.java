package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class ManifestDto {
    private Config config;
    @Data @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Config {
        private String digest;
    }
}
