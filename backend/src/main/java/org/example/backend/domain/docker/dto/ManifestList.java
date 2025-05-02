package org.example.backend.domain.docker.dto;

import lombok.Data;

import java.util.List;

@Data
public class ManifestList {
    private int schemaVersion;
    private String mediaType;
    private List<ManifestDescriptor> manifests;
}
