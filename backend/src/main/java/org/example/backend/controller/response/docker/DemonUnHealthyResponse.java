package org.example.backend.controller.response.docker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DemonUnHealthyResponse {
    private String Image;
    private String ImageId;
}
