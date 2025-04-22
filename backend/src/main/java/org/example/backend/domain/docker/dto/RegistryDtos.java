package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

public class RegistryDtos {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Manifest {
        @Data
        public static class Config {
            private String digest;
        }
        private Config config;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConfigBlob {
        @JsonProperty("config")
        private Config config;

        @Data
        public static class Config {
            @JsonProperty("ExposedPorts")
            private Map<String, Object> exposedPorts;
        }
    }
}
