package org.example.backend.controller.response.jenkins;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JenkinsBuildStepResponse {
    private int stepNumber;
    private String stepName;
    private String duration;
    private String status;
    private List<JenkinsBuildEchoResponse> echoList;
}