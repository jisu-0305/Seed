package org.example.backend.domain.jenkins.service;

import org.example.backend.controller.response.jenkins.JenkinsBuildChangeResponse;
import org.example.backend.controller.response.jenkins.JenkinsBuildChangeSummaryResponse;
import org.example.backend.controller.response.jenkins.JenkinsBuildDetailResponse;
import org.example.backend.controller.response.jenkins.JenkinsBuildListResponse;

import java.util.List;

public interface JenkinsService {
    List<JenkinsBuildListResponse> getBuildList();
    JenkinsBuildListResponse getLastBuild();
    JenkinsBuildDetailResponse getBuildDetail(int buildNumber);
    String getBuildLog(int buildNumber);
    String getBuildStatus(int buildNumber);
    void triggerBuild();
    List<JenkinsBuildChangeResponse> getBuildChanges(int buildNumber);
    List<JenkinsBuildChangeSummaryResponse> getBuildChangesWithSummary(int buildNumber);
    public void logLastBuildResultToProject(Long projectId);
}