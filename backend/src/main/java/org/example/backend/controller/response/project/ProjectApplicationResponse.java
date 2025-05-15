package org.example.backend.controller.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProjectApplicationResponse {
    private String imageName;
    private List<Integer> defaultPorts;
    private Long applicationId;
    private String description;
}
