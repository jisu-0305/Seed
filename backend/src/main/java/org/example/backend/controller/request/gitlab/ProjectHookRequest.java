package org.example.backend.controller.request.gitlab;

import lombok.Getter;

@Getter
public class ProjectHookRequest {
    private String url;
    private String wildcard;
}
