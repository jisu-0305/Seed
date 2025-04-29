package org.example.backend.controller.response.jenkins;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JenkinsBuildListResponse {
    private int buildNumber;
    private String buildName;
    private String date;
    private String time;
    private String status;
}
