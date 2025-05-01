package org.example.backend.controller.response.docker;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemonContainerStateCountResponse {

    // 이건 정상 동작하는 컨테이너 개수 -> 나중에 필요하면 쓰기.
    @JsonProperty("ContainersRunning")
    private int containersRunning;

    @JsonProperty("ContainersPaused")
    private int containersPaused;

    @JsonProperty("ContainersStopped")
    private int containersStopped;

}
