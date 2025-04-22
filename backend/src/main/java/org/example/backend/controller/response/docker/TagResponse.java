package org.example.backend.controller.response.docker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.example.backend.domain.docker.dto.DockerTag;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagResponse {
    private int count; // 전체 태그 수
    private String next; // 다음page 조회하는 url(페이지 없으면 null)
    private String previous; // 이전page 조회하는 url
    private List<DockerTag> results; // 현재 페이지에 포함된 태그 객체들
}
