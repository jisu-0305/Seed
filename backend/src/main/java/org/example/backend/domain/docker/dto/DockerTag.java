package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerTag {
    private int count; // 전체 태그 수
    private String next; // 다음페이지 조회 URL (페이지 없으면 null)
    private String previous; // 이전페이지 조회 URL
    private List<DockerTagItems> results; // 현재 페이지에 포함된 태그 객체들
}
