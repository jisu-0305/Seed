package org.example.backend.controller.response.jenkins;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JenkinsBuildChangeSummaryResponse {
    private String commitId;
    private String author;
    private String message;
    private String timestamp;
    private List<String> modifiedFileList;
}
