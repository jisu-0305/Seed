package org.example.backend.controller.response.docker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DemonHealthyCheckResponse {
    private String Image;
    private String ImageId;
}
