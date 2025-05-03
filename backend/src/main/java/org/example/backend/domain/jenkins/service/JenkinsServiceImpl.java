package org.example.backend.domain.jenkins.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.response.jenkins.*;
import org.example.backend.domain.project.entity.ProjectExecution;
import org.example.backend.domain.project.entity.ProjectStatus;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.project.enums.ExecutionType;
import org.example.backend.domain.project.repository.ProjectExecutionRepository;
import org.example.backend.domain.project.repository.ProjectStatusRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JenkinsServiceImpl implements JenkinsService {

    private final JenkinsClient jenkinsClient;
    private final ObjectMapper objectMapper;
    private ProjectStatusRepository projectStatusRepository;
    private ProjectExecutionRepository projectExecutionRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy. M. d. a hh:mm:ss")
            .withZone(ZoneId.systemDefault())
            .withLocale(Locale.KOREA);

    @Value("${jenkins.job-name}")
    private String jobName;

    @Override
    public List<JenkinsBuildListResponse> getBuildList() {
        JsonNode builds = safelyParseJson(jenkinsClient.fetchBuildInfo(jobName, "api/json?tree=builds[number,result,timestamp]"))
                .path("builds");

        List<JenkinsBuildListResponse> list = new ArrayList<>();
        for (JsonNode build : builds) {
            list.add(JenkinsBuildListResponse.builder()
                    .buildNumber(build.path("number").asInt())
                    .buildName("MR 빌드")
                    .date(DATE_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                    .time(TIME_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                    .status(build.path("result").asText())
                    .build());
        }
        return list;
    }

    @Override
    public JenkinsBuildListResponse getLastBuild() {
        JsonNode build = safelyParseJson(jenkinsClient.fetchBuildInfo(jobName, "lastBuild/api/json"));

        return JenkinsBuildListResponse.builder()
                .buildNumber(build.path("number").asInt())
                .buildName("MR 빌드")
                .date(DATE_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                .time(TIME_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                .status(build.path("result").asText())
                .build();
    }

    @Override
    public JenkinsBuildDetailResponse getBuildDetail(int buildNumber) {
        JsonNode buildInfo = safelyParseJson(jenkinsClient.fetchBuildInfo(jobName, buildNumber + "/api/json"));
        String consoleLog = jenkinsClient.fetchBuildLog(jobName, buildNumber);

        return JenkinsBuildDetailResponse.builder()
                .buildNumber(buildNumber)
                .buildName("MR 빌드")
                .overallStatus(buildInfo.path("result").asText())
                .stepList(parseConsoleLog(consoleLog))
                .build();
    }

    @Override
    public String getBuildLog(int buildNumber) {
        return jenkinsClient.fetchBuildLog(jobName, buildNumber);
    }

    @Override
    public String getBuildStatus(int buildNumber) {
        JsonNode build = safelyParseJson(jenkinsClient.fetchBuildInfo(jobName, buildNumber + "/api/json"));
        return build.path("result").asText();
    }

    @Override
    public void triggerBuild() {
        jenkinsClient.triggerBuild(jobName);
    }

    @Override
    public List<JenkinsBuildChangeResponse> getBuildChanges(int buildNumber) {
        JsonNode root = safelyParseJson(jenkinsClient.fetchBuildInfo(jobName, buildNumber + "/api/json?tree=changeSets[items[commitId,author[fullName],msg,timestamp]]"));
        JsonNode changeSets = root.path("changeSets");

        List<JenkinsBuildChangeResponse> changes = new ArrayList<>();
        for (JsonNode set : changeSets) {
            for (JsonNode item : set.path("items")) {
                changes.add(JenkinsBuildChangeResponse.builder()
                        .commitId(item.path("commitId").asText())
                        .author(item.path("author").path("fullName").asText())
                        .message(item.path("msg").asText())
                        .timestamp(formatJenkinsTimestamp(item.path("timestamp").asLong()))
                        .build());
            }
        }
        return changes;
    }

    @Override
    public List<JenkinsBuildChangeSummaryResponse> getBuildChangesWithSummary(int buildNumber) {
        JsonNode root = safelyParseJson(jenkinsClient.fetchBuildInfo(jobName, buildNumber + "/api/json?tree=changeSets[items[commitId,author[fullName],msg,timestamp,paths[file]]]"));
        JsonNode changeSets = root.path("changeSets");

        if (changeSets.isMissingNode() || changeSets.isEmpty()) {
            return Collections.emptyList();
        }

        List<JenkinsBuildChangeSummaryResponse> summaries = new ArrayList<>();
        for (JsonNode set : changeSets) {
            for (JsonNode item : set.path("items")) {
                List<String> modifiedFiles = new ArrayList<>();
                for (JsonNode path : item.path("paths")) {
                    modifiedFiles.add(path.path("file").asText());
                }

                summaries.add(JenkinsBuildChangeSummaryResponse.builder()
                        .commitId(item.path("commitId").asText())
                        .author(item.path("author").path("fullName").asText())
                        .message(item.path("msg").asText())
                        .timestamp(formatJenkinsTimestamp(item.path("timestamp").asLong()))
                        .modifiedFileList(modifiedFiles)
                        .build());
            }
        }
        return summaries;
    }

    @Override
    @Transactional
    public void logLastBuildResultToProject(Long projectId) {
        JsonNode lastBuild = safelyParseJson(
                jenkinsClient.fetchBuildInfo(jobName, "lastBuild/api/json")
        );

        int buildNumber = lastBuild.path("number").asInt();
        BuildStatus status = BuildStatus.valueOf(lastBuild.path("result").asText());

        ProjectStatus statusEntity = projectStatusRepository.findByProjectId(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_STATUS_NOT_FOUND));

        statusEntity.updateBuildStatus(status);

        projectExecutionRepository.save(ProjectExecution.builder()
                .projectId(projectId)
                .executionType(ExecutionType.BUILD)
                .projectExecutionTitle("#" + buildNumber + " MR 빌드")
                .executionStatus(status)
                .buildNumber(String.valueOf(buildNumber))
                .createdAt(LocalDate.now())
                .build());
    }

    private List<JenkinsBuildStepResponse> parseConsoleLog(String consoleLog) {
        List<JenkinsBuildStepResponse> steps = new ArrayList<>();
        String[] lines = consoleLog.split("\n");

        int stepNumber = 1;
        int echoNumber = 1;
        String currentStageName = null;
        String currentStageStartTime = null;
        List<JenkinsBuildEchoResponse> currentEchoes = new ArrayList<>();

        boolean expectingEchoContent = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            String timestamp = null;
            if (line.matches("^\\[\\d{2}:\\d{2}:\\d{2}]\\s.*")) {
                timestamp = line.substring(1, 9);
                line = line.substring(10);
            }

            if (expectingEchoContent) {
                if (!line.isBlank()) {
                    currentEchoes.add(JenkinsBuildEchoResponse.builder()
                            .echoNumber(echoNumber++)
                            .echoContent(line.trim())
                            .duration("-")
                            .build());
                }
                expectingEchoContent = false;
                continue;
            }

            if (line.startsWith("[Pipeline] echo")) {
                expectingEchoContent = true;
                continue;
            }

            if (line.startsWith("[Pipeline] { (")) {
                int startIdx = line.indexOf('(');
                int endIdx = line.indexOf(')');
                if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                    if (currentStageName != null) {
                        steps.add(createStep(stepNumber++, currentStageName, currentStageStartTime, timestamp, currentEchoes));
                    }
                    currentStageName = line.substring(startIdx + 1, endIdx).trim();
                    currentStageStartTime = timestamp;
                    currentEchoes.clear();
                    echoNumber = 1;
                }
            }
        }

        if (currentStageName != null) {
            steps.add(createStep(stepNumber, currentStageName, currentStageStartTime, null, currentEchoes));
        }

        return steps;
    }

    private JenkinsBuildStepResponse createStep(int stepNumber, String stageName, String start, String end, List<JenkinsBuildEchoResponse> echoes) {
        String duration = "-";
        if (start != null && end != null) {
            long durationSeconds = calculateDuration(start, end);
            duration = formatDuration(durationSeconds);
        }

        return JenkinsBuildStepResponse.builder()
                .stepNumber(stepNumber)
                .stepName(stageName)
                .duration(duration)
                .status("SUCCESS")
                .echoList(new ArrayList<>(echoes))
                .build();
    }

    private long calculateDuration(String start, String end) {
        try {
            LocalTime startTime = LocalTime.parse(start, TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(end, TIME_FORMATTER);
            if (endTime.isBefore(startTime)) {
                endTime = endTime.plusHours(24);
            }
            return Duration.between(startTime, endTime).getSeconds();
        } catch (Exception e) {
            return -1;
        }
    }

    private String formatDuration(long seconds) {
        if (seconds < 0) {
            return "-";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return (minutes > 0 ? minutes + "m " : "") + remainingSeconds + "s";
    }

    private String formatJenkinsTimestamp(long timestampMillis) {
        return FULL_TIME_FORMATTER.format(Instant.ofEpochMilli(timestampMillis));
    }

    private JsonNode safelyParseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JENKINS_RESPONSE_PARSING_FAILED);
        }
    }

    private BuildStatus convertToBuildStatus(String result) {
        return BuildStatus.valueOf(result);
    }
}
