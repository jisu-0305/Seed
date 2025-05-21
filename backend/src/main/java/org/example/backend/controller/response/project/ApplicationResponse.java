package org.example.backend.controller.response.project;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApplicationResponse {
    private Long applicationId;
    private String imageName;
    private String tag;
    private int port;
    private List<String> imageEnvs;
}
