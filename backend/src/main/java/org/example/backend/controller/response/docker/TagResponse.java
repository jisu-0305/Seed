package org.example.backend.controller.response.docker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagResponse {
    private String name;
    private long repository;
    private boolean v2;
    private String digest;
}
