package org.example.backend.domain.gitlab.service;

public interface GitlabDeployService {
    void appendNewlineToReadme() throws Exception;
    void commitAndPush() throws Exception;
    void createMergeRequest();
}