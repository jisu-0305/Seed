package org.example.backend.controller.response.jenkins;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JenkinsBuildDetailResponse {
    private int buildNumber;
    private String buildName;
    private String overallStatus;
    private List<JenkinsBuildStepResponse> stepList;
}