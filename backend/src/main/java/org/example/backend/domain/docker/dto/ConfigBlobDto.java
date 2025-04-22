package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigBlobDto {
    private Config config;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Config {
        // 제이슨에는 "ExposedPorts": { ... } 로 되어 있으니깐 대소문자 구분 없이 매핑되도록
        @JsonProperty("ExposedPorts")
        private Map<String, Object> exposedPorts;
    }
}
