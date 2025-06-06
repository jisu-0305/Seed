package org.example.backend.util.aiapi.dto.suspectapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class InferAppResponse {
    @JsonProperty("Reason")
    private String reason;
    private List<String> suspectedApps;
}

