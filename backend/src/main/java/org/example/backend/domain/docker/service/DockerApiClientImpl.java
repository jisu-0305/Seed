package org.example.backend.domain.docker.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.DockerUriBuilder;
import org.example.backend.controller.response.docker.SearchResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.domain.docker.dto.*;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;


@Component
@Slf4j
@RequiredArgsConstructor
public class DockerApiClientImpl implements DockerApiClient {

    private final WebClient dockerHubWebClient;
    private final WebClient dockerAuthWebClient;
    private final WebClient dockerRegistryWebClient;
    private final DockerUriBuilder uriBuilder;

    @Override
    public SearchResponse search(String query, int page, int pageSize) {
        try {
            return dockerHubWebClient.get()
                    .uri(uriBuilder.searchRepositories(query, page, pageSize))
                    .retrieve()
                    .bodyToMono(SearchResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Docker search API error [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.DOCKER_SEARCH_FAILED);
        } catch (Exception e) {
            log.error("Unexpected error calling Docker search API", e);
            throw new BusinessException(ErrorCode.DOCKER_SEARCH_FAILED);
        }
    }

    @Override
    public TagResponse listTags(String namespace, String repo, int page, int pageSize) {
        try {
            return dockerHubWebClient.get()
                    .uri(uriBuilder.listTags(namespace, repo, page, pageSize))
                    .retrieve()
                    .bodyToMono(TagResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Docker listTags API error [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.DOCKER_TAGS_FAILED);
        } catch (Exception e) {
            log.error("Unexpected error calling Docker listTags API", e);
            throw new BusinessException(ErrorCode.DOCKER_TAGS_FAILED);
        }
    }

    @Override
    public List<String> getExposedPorts(String namespace, String repo, String tag, String os, String arch) {
        try {
            // 1) 토큰 발급
            String token = dockerAuthWebClient.get()
                    .uri(uriBuilder.tokenUri(namespace, repo))
                    .retrieve()
                    .bodyToMono(DockerAuthToken.class)
                    .block()
                    .getToken();

            // 2) 매니페스트 인덱스 조회 (멀티-플랫폼 지원)
            ManifestIndexDto index = dockerRegistryWebClient.get()
                    .uri(uriBuilder.manifestUri(namespace, repo, tag))
                    .headers(h -> h.setBearerAuth(token))
                    .header(HttpHeaders.ACCEPT, "application/vnd.oci.image.index.v1+json")
                    .retrieve()
                    .bodyToMono(ManifestIndexDto.class)
                    .block();

            String configDigest;
            if (index != null && index.getManifests() != null) {
                // 2‑1) 요청된 os/arch에 맞는 매니페스트 선택
                String chosenDigest = index.getManifests().stream()
                        .filter(m ->
                                os.equalsIgnoreCase(m.getPlatform().getOs()) &&
                                        arch.equalsIgnoreCase(m.getPlatform().getArchitecture())
                        )
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.DOCKER_PORTS_FAILED))
                        .getDigest();

                // 2‑2) 단일 매니페스트(v2) 조회
                ManifestDto manifest = dockerRegistryWebClient.get()
                        .uri(uriBuilder.manifestByDigestUri(namespace, repo, chosenDigest))
                        .headers(h -> h.setBearerAuth(token))
                        .header(HttpHeaders.ACCEPT, "application/vnd.docker.distribution.manifest.v2+json")
                        .retrieve()
                        .bodyToMono(ManifestDto.class)
                        .block();

                configDigest = manifest.getConfig().getDigest();
            } else {
                // 단일 매니페스트(tag가 인덱스가 아니라면)
                ManifestDto manifest = dockerRegistryWebClient.get()
                        .uri(uriBuilder.manifestUri(namespace, repo, tag))
                        .headers(h -> h.setBearerAuth(token))
                        .header(HttpHeaders.ACCEPT, "application/vnd.docker.distribution.manifest.v2+json")
                        .retrieve()
                        .bodyToMono(ManifestDto.class)
                        .block();

                configDigest = manifest.getConfig().getDigest();
            }

            // 3) Config Blob 조회 (raw JSON 또는 gzip JSON)
            byte[] blobBytes = dockerRegistryWebClient.get()
                    .uri(uriBuilder.blobUri(namespace, repo, configDigest))
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (blobBytes == null || blobBytes.length == 0) {
                return Collections.emptyList();
            }

            // 4) GZIP 여부 확인 후 JSON 문자열로 변환
            String json;
            if (blobBytes.length > 2
                    && (blobBytes[0] == (byte)0x1f && blobBytes[1] == (byte)0x8b)) {
                // GZIP 매직 넘버(0x1f8b)이면 압축 해제
                try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(blobBytes))) {
                    json = new String(gis.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else {
                // 아니면 그냥 UTF-8 문자열
                json = new String(blobBytes, StandardCharsets.UTF_8);
            }

            // 5) Jackson으로 파싱
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ConfigBlobDto config = mapper.readValue(json, ConfigBlobDto.class);

            Map<String,Object> ports = config.getConfig().getExposedPorts();
            return ports != null
                    ? new ArrayList<>(ports.keySet())
                    : Collections.emptyList();

        } catch (Exception e) {
            log.error("Error fetching ports for {}/{}:{} [{} / {}]", namespace, repo, tag, os, arch, e);
            throw new BusinessException(ErrorCode.DOCKER_PORTS_FAILED);
        }
    }


}