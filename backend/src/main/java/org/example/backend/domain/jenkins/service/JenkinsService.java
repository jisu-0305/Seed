package org.example.backend.domain.jenkins.service;

import org.example.backend.controller.response.jenkins.*;

import java.util.List;

public interface JenkinsService {
    JenkinsBuildPageResponse getBuildList(Long projectId, int start, int limit, String accessToken);
    JenkinsBuildListResponse getLastBuild(Long projectId, String accessToken);
    JenkinsBuildDetailResponse getBuildDetail(int buildNumber, Long projectId, String accessToken);
    String getBuildLog(int buildNumber, Long projectId, String accessToken);
    String getBuildStatus(int buildNumber, Long projectId, String accessToken);
    void triggerBuild(Long projectId, String accessToken, String branchName);
    List<JenkinsBuildChangeResponse> getBuildChanges(int buildNumber, Long projectId, String accessToken);
    List<JenkinsBuildChangeSummaryResponse> getBuildChangesWithSummary(int buildNumber, Long projectId, String accessToken);
    void logLastBuildResultToProject(Long projectId, String accessToken);
    void issueAndSaveToken(Long projectId, String serverIp, String accessToken);
    String getStepLogById(Long projectId, int buildNumber, String stepNumber, String accessToken);
}