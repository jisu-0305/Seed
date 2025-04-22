package org.example.backend.domain.docker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagImage {
    private String architecture; // 빌드된 cpu 아키텍처 : amd64, arm64, ppc64le...
    private String features; // 아키텍처 특성 (예: RISC‑V 등), 없으면 빈 문자열
    private String variant; //아키텍처 변종 (예: v7 등), 없으면 null
    private String digest; // 해당 이미지 레이어의 SHA256 해시
    private String os; //운체 : linux, windows...

    @JsonProperty("os_features")
    private String osFeatures;

    @JsonProperty("os_version")
    private String osVersion;  // 운채 버전_보통 null

    private long size; // 이미지 압축 크기(바이트)
    private String status; //이미지 상태 : active...

    @JsonProperty("last_pulled")
    private String lastPulled;

    @JsonProperty("last_pushed")
    private String lastPushed;
}
