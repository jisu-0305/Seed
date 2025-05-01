package org.example.backend.controller.response.docker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppHealthyCheckResponse {
    private String containerName;
    private String Image;
    private String ImageId;
    private String State;
    private String Status;
}
