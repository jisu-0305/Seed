package org.example.backend.domain.jenkins.service;

import org.example.backend.domain.jenkins.entity.JenkinsInfo;

public interface JenkinsClient {
    String fetchBuildInfo(JenkinsInfo info, String path);
    String fetchBuildLog(JenkinsInfo info, int buildNumber);
    void triggerBuild(JenkinsInfo info);
}
