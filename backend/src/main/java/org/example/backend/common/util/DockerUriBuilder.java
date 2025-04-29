package org.example.backend.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DockerUriBuilder {

    @Value("${docker.hub.api.base-url}")
    private String dockerHubBaseUrl;

    @Value("${docker.engine.api.base-url}")
    private String dockerEngineApiBaseUrl;

//    @Value("${docker.registry.api.base-url}")
//    private String registryBaseUrl;

    public String searchRepositories(String query, int page, int pageSize) {
        return UriComponentsBuilder.fromUriString(dockerHubBaseUrl)
                .path("/search/repositories")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .toUriString();
    }

    public String listTags(String namespace, String repo, int page, int pageSize) {
        return UriComponentsBuilder.fromUriString(dockerHubBaseUrl)
                .path("/repositories")
                .pathSegment(namespace, repo, "tags")
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .toUriString();
    }

    public String info() {
        return "/info";
    }

    public String containersByStatus(List<String> statuses) {
        String json = String.format(
                "{\"status\":[%s]}",
                statuses.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","))
        );
        String encoded = URLEncoder.encode(json, StandardCharsets.UTF_8);
        return "/containers/json?filters=" + encoded;
    }

    public String containersByName(String nameFilter) {
        String json = String.format("{\"name\":[\"%s\"]}", nameFilter);
        String encoded = URLEncoder.encode(json, StandardCharsets.UTF_8);
        return "/containers/json?filters=" + encoded;
    }

//    public String info() {
//        return UriComponentsBuilder.fromUriString(dockerEngineApiBaseUrl)
//                .path("/info")
//                .toUriString();
//    }

//    public String containersByStatus(List<String> statuses) {
//        String jsonFilter = String.format(
//                "{\"status\":[%s]}",
//                statuses.stream()
//                        .map(s -> "\"" + s + "\"")
//                        .collect(Collectors.joining(","))
//        );
//        return UriComponentsBuilder.fromUriString(dockerEngineApiBaseUrl)
//                .path("/containers/json")
//                .queryParam("filters", jsonFilter)
//                .toUriString();
//    }
//
//    public String containersByName(String nameFilter) {
//        String filter = String.format("{\"name\":[\"%s\"]}", nameFilter);
//        return UriComponentsBuilder.fromUriString(dockerEngineApiBaseUrl)
//                .path("/containers/json")
//                .queryParam("filters", filter)
//                .toUriString();
//    }

}
