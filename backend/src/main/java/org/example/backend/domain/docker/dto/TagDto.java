package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagDto {
    private long creator;
    private long id; // 태그 id
    private List<ImageDto> images; //해당 태그에 매핑된 아키텍처별 이미지 정보 리스트

    @JsonProperty("last_updated")
    private String lastUpdated;

    @JsonProperty("last_updater")
    private long lastUpdater;

    @JsonProperty("last_updater_username")
    private String lastUpdaterUsername;

    private String name; // 태그 이름
    private long repository;

    @JsonProperty("full_size")
    private long fullSize;

    private boolean v2;

    @JsonProperty("tag_status")
    private String tagStatus;

    @JsonProperty("tag_last_pulled")
    private String tagLastPulled;

    @JsonProperty("tag_last_pushed")
    private String tagLastPushed;

    @JsonProperty("media_type")
    private String mediaType; // 매니페스트의 미디어 타입 (application/vnd.oci.image.index.v1+json)

    @JsonProperty("content_type")
    private String contentType; //콘텐츠 유형 : image...

    private String digest; //매니페스트 sha256해시
}
