package org.example.backend.domain.docker.dto;

import lombok.Data;

@Data
public class Platform {
    private String architecture; //  cpu관련 : amd64...
    private String os; // linux...
    private String variant;
}
