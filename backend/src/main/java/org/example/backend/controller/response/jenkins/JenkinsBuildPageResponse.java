package org.example.backend.controller.response.jenkins;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class JenkinsBuildPageResponse {
    private List<JenkinsBuildListResponse> builds;
    private boolean hasNext;
    private Integer nextStart;
}