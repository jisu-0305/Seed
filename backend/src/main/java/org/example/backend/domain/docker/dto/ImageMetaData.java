package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageMetaData {
    @JsonProperty("config")
    private ImageBlobDetail imageBlobHashInfo;

    @JsonProperty("manifests")
    private List<ImagePlatformAndId> additionalImagePlatformAndId;
}
