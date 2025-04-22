package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.controller.response.docker.TagResponse;
import org.example.backend.domain.docker.service.DockerService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docker")
@RequiredArgsConstructor
@Slf4j
public class DockerController {

    private final DockerService dockerService;

    @GetMapping("/images")
    public ResponseEntity<ApiResponse<ImageResponse>> searchDockerImages(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        ImageResponse response = dockerService.getImages(query, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/images/{namespace}/{image}/tags")
    public ResponseEntity<ApiResponse<TagResponse>> searchDockerImageTags(
            @PathVariable String namespace,
            @PathVariable String image,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        TagResponse response = dockerService.getTags(namespace, image, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

//    @GetMapping("/repositories/{ns}/{repo}/tags/{tag}/ports")
//    public ResponseEntity<ApiResponse<List<String>>> getPorts(
//            @PathVariable String ns,
//            @PathVariable String repo,
//            @PathVariable String tag,
//            @RequestParam(defaultValue = "linux") String os,
//            @RequestParam(defaultValue = "amd64") String arch
//    ) {
//        List<String> ports = dockerService.getDefaultPorts(ns, repo, tag, os, arch);
//        return ResponseEntity.ok(ApiResponse.success(ports));
//    }
}
