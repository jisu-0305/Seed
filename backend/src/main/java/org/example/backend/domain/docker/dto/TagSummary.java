package org.example.backend.domain.docker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagSummary {
    private String name;
    private long   repository;
    private boolean v2;
    private String digest;
}
