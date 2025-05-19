package org.example.backend.domain.jenkins.service;

import org.example.backend.controller.response.jenkins.*;
import org.example.backend.domain.aireport.enums.ReportStatus;

import java.util.List;

public interface JenkinsService {
    JenkinsBuildPageResponse getBuildList(Long projectId, int start, int limit, String accessToken);
    int getLastBuildNumberWithOutLogin(Long projectId);
    JenkinsBuildListResponse getLastBuild(Long projectId, String accessToken);
    JenkinsBuildDetailResponse getBuildDetail(int buildNumber, Long projectId, String accessToken);
    String getBuildLogWithOutLogin(int buildNumber, Long projectId);
    String getBuildLog(int buildNumber, Long projectId, String accessToken);
    String getBuildStatusWithOutLogin(int buildNumber, Long projectId);
    String getBuildStatus(int buildNumber, Long projectId, String accessToken);
    void triggerBuildWithOutLogin(Long projectId, String branchName, String originalBranchName);
    void triggerBuild(Long projectId, String accessToken, String branchName);
    List<JenkinsBuildChangeResponse> getBuildChanges(int buildNumber, Long projectId, String accessToken);
    List<JenkinsBuildChangeSummaryResponse> getBuildChangesWithSummary(int buildNumber, Long projectId, String accessToken);
    void logLastBuildResultToProject(Long projectId);
    void issueAndSaveToken(Long projectId, String serverIp, String accessToken);
    String getStepLogById(Long projectId, int buildNumber, String stepNumber, String accessToken);
    ReportStatus waitUntilBuildFinishes(int newBuildNumber, Long projectId);
}