package org.example.backend.common.util;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DockerUriBuilder {

    @Value("${docker.hub.api.base-url}")
    private String dockerHubBaseUrl;

    @Value("${docker.engine.api-port}")
    private int engineApiPort;

    public URI buildSearchRepositoriesUri(String query, int page, int pageSize) {
        return UriComponentsBuilder.fromUriString(dockerHubBaseUrl)
                .path("/search/repositories")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .build()
                .toUri();
    }

    public URI buildHubTagsUri(String namespace, String repo, int page, int pageSize) {
        return UriComponentsBuilder.fromUriString(dockerHubBaseUrl)
                .pathSegment("repositories", namespace, repo, "tags")
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .build()
                .toUri();
    }

    public URI buildInfoUri(String serverIp) {
        String baseUrl = String.format("http://%s:%d", serverIp, engineApiPort);
        return UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/info")
                .build()
                .toUri();
    }

    public URI buildContainersByStatusUri(String serverIp, List<String> statuses) {

        String baseUrl = String.format("http://%s:%d", serverIp, engineApiPort);

        String json = String.format(
                "{\"status\":[%s]}",
                statuses.stream()
                        .map(eachStatus -> "\"" + eachStatus + "\"")
                        .collect(Collectors.joining(","))
        );

        String filters = URLEncoder.encode(json, StandardCharsets.UTF_8);

        String uriString = String.format("%s/containers/json?all=true&filters=%s", baseUrl, filters);
        return URI.create(uriString);
    }

    public URI buildContainersByNameUri(String serverIp, String name) {

        String baseUrl = String.format("http://%s:%d", serverIp, engineApiPort);

        String jsonFilter = String.format("{\"name\":[\"%s\"]}", name);
        String encodedFilter = URLEncoder.encode(jsonFilter, StandardCharsets.UTF_8);

        String uriStr = String.format("%s/containers/json?all=true&filters=%s", baseUrl, encodedFilter);
        return URI.create(uriStr);
    }

    public URI buildContainerLogsUri(String serverIp, String containerId, DockerContainerLogRequest req) {

        String baseUrl = String.format("http://%s:%d", serverIp, engineApiPort);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/containers/{id}/logs")
                .queryParam("stdout", true)
                .queryParam("stderr", true)
                .queryParam("timestamps", true);

        if (req.sinceSeconds() != null) {
            builder.queryParam("since", req.sinceSeconds());
        }
        if (req.untilSeconds() != null) {
            builder.queryParam("until", req.untilSeconds());
        }

        return builder.buildAndExpand(containerId).toUri();
    }

}
