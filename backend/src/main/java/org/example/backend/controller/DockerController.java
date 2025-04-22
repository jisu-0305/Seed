package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.response.docker.SearchResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docker")
@RequiredArgsConstructor
@Slf4j
public class DockerController {

    private final DockerService dockerService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponse>> searchRepositories(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        SearchResponse response = dockerService.searchRepositories(query, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/repositories/{namespace}/{repo}/tags")
    public ResponseEntity<ApiResponse<TagResponse>> listTags(
            @PathVariable String namespace,
            @PathVariable String repo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize
    ) {
        TagResponse response = dockerService.getTags(namespace, repo, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/repositories/{ns}/{repo}/tags/{tag}/ports")
    public ResponseEntity<ApiResponse<List<String>>> getPorts(
            @PathVariable String ns,
            @PathVariable String repo,
            @PathVariable String tag,
            @RequestParam(defaultValue = "linux") String os,
            @RequestParam(defaultValue = "amd64") String arch
    ) {
        List<String> ports = dockerService.getDefaultPorts(ns, repo, tag, os, arch);
        return ResponseEntity.ok(ApiResponse.success(ports));
    }
}
