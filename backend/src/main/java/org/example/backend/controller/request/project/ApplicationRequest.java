package org.example.backend.controller.request.project;

import lombok.Getter;

@Getter
public class ApplicationRequest {
    private String name;
    private String tag;
    private int port;
}
