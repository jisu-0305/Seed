package org.example.backend.domain.docker.dto;

import lombok.Data;

@Data
public class ManifestDescriptor {
    private String mediaType;
    private long size;
    private String digest;
    private Platform platform;
}
