package org.example.backend.controller.request.project;

import lombok.Getter;

import java.util.List;

@Getter
public class ProjectUpdateRequest {
    private String serverIP;
    private List<ApplicationRequest> applications;
}
