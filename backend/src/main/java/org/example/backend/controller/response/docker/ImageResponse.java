package org.example.backend.controller.response.docker;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.example.backend.domain.docker.dto.DockerImage;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageResponse {
    private int count;
    private String next;
    private String previous;

    @JsonAlias("results")
    private List<DockerImage> image;
}
