package org.example.backend.controller.response.jenkins;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JenkinsBuildEchoResponse {
    private int echoNumber;
    private String echoContent;
    private String duration;
}

