package org.example.backend.controller.response.docker;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemonInfoResponse {
    @JsonProperty("ContainersPaused")
    private int containersPaused;

    @JsonProperty("ContainersStopped")
    private int containersStopped;
}
