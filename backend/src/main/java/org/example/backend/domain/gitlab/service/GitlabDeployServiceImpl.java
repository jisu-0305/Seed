package org.example.backend.domain.gitlab.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GitlabDeployServiceImpl implements GitlabDeployService {

    @Value("${deploy.repo.path}")
    private String repoPath;

    @Value("${deploy.branch}")
    private String branch;

    @Value("${gitlab.access-token}")
    private String accessToken;

    @Value("${gitlab.project-id}")
    private String projectId;

    @Override
    public void appendNewlineToReadme() throws IOException {
        Path readmePath = Paths.get(repoPath, "README.md");

        if (!Files.exists(readmePath)) {
            Files.write(readmePath, List.of("# README\n"), StandardOpenOption.CREATE_NEW);
            log.info("✅ README.md가 없어서 새로 생성했습니다.");
        } else {
            Files.write(readmePath, List.of("<!-- trigger deployment -->"), StandardOpenOption.APPEND);
            log.info("📄 README.md에 배포용 트리거 라인을 추가했습니다.");
        }

    }

    @Override
    public void commitAndPush() throws IOException, InterruptedException {
        run("git", "add", "README.md");
        run("git", "commit", "-m", "🔄 trigger deployment");
        run("git", "push", "origin", branch);
    }

    @Override
    public void createMergeRequest() {
        WebClient.create("https://lab.ssafy.com/api/v4")
                .post()
                .uri("/projects/{projectId}/merge_requests", projectId)
                .header("PRIVATE-TOKEN", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "source_branch", branch,
                        "target_branch", "dev",
                        "title", "🔁 자동 배포용 MR"
                ))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("📬 MR 생성 응답: {}", response))
                .block();
    }

    private void run(String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(repoPath));
        pb.inheritIO();
        Process process = pb.start();
        int result = process.waitFor();
        if (result != 0) {
            throw new RuntimeException("Git 명령 실패: " + String.join(" ", command));
        }
    }
}