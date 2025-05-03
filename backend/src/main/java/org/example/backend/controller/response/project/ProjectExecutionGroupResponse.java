package org.example.backend.controller.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectExecutionGroupResponse {
    private LocalDate date;
    private List<ProjectExecutionResponse> executionList;
}