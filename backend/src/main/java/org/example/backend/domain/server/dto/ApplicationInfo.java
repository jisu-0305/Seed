package org.example.backend.domain.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class ApplicationInfo {

    @NotBlank
    private String image;       // ex: "nginx", "myorg/myapp"

    @NotBlank
    private String tag;         // ex: "latest", "1.24.0"

    @NotEmpty
    private int port; // ex: [8080, 8443]
}
