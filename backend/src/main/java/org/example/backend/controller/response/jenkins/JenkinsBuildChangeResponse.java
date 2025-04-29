package org.example.backend.controller.response.jenkins;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JenkinsBuildChangeResponse {
    private String commitId;
    private String author;
    private String message;
    private String timestamp;
}