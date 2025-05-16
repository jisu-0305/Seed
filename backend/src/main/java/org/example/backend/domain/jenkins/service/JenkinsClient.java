package org.example.backend.domain.jenkins.service;

import org.example.backend.domain.jenkins.entity.JenkinsInfo;

public interface JenkinsClient {
    String fetchBuildInfo(JenkinsInfo info, String path);
    String fetchBuildLog(JenkinsInfo info, int buildNumber);
    void triggerBuildWithoutLogin(JenkinsInfo info, String branchName, String originalBranchName);
    void triggerBuild(JenkinsInfo info, String branchName);
}
