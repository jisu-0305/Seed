package org.example.backend.controller.response.aireport;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeploymentReportListResponse {
    private List<DeploymentReportResponse> reports;
}
