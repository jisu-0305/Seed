package org.example.backend.controller.request.gitlab;

import lombok.Getter;

@Getter
public class ProjectHookRequest {
    private Long projectId;
    private String url;
    private String wildcard;
}
