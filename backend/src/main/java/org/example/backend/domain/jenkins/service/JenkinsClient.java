package org.example.backend.domain.jenkins.service;

public interface JenkinsClient {
    String fetchBuildInfo(String jobName, String path);
    String fetchBuildLog(String jobName, int buildNumber);
    void triggerBuild(String jobName);
}
