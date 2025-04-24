package org.example.backend.controller.response.project;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationResponse {
    private String name;
    private String tag;
    private int port;
}
