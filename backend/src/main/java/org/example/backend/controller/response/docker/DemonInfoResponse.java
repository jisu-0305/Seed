package org.example.backend.controller.response.docker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DemonInfoResponse {
    private int containersPaused;
    private int containersStopped;
}
