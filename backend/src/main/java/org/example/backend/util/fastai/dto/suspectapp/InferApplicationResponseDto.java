package org.example.backend.util.fastai.dto.suspectapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class InferApplicationResponseDto {
    @JsonProperty("Reason")
    private String reason;
    private List<String> suspectedApps;
}

