package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ContainerDto {

    @JsonProperty("Names")
    private List<String> names;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Image")
    private String image;

    @JsonProperty("ImageID")
    private String imageId;

    @JsonProperty("State")
    private String state;

    @JsonProperty("Status")
    private String status;
}
