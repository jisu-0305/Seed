package org.example.backend.domain.jenkins.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.response.jenkins.*;
import org.example.backend.domain.jenkins.entity.JenkinsInfo;
import org.example.backend.domain.jenkins.repository.JenkinsInfoRepository;
import org.example.backend.domain.project.entity.ProjectExecution;
import org.example.backend.domain.project.entity.ProjectStatus;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.project.enums.ExecutionType;
import org.example.backend.domain.project.repository.ProjectExecutionRepository;
import org.example.backend.domain.project.repository.ProjectStatusRepository;
import org.example.backend.domain.userproject.repository.UserProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JenkinsServiceImpl implements JenkinsService {

    private final JenkinsClient jenkinsClient;
    private final ObjectMapper objectMapper;
    private ProjectStatusRepository projectStatusRepository;
    private ProjectExecutionRepository projectExecutionRepository;
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final UserProjectRepository userProjectRepository;
    private final RedisSessionManager redisSessionManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy. M. d. a hh:mm:ss")
            .withZone(ZoneId.systemDefault())
            .withLocale(Locale.KOREA);

    @Override
    public List<JenkinsBuildListResponse> getBuildList(Long projectId, String accessToken) {
        validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode builds = safelyParseJson(jenkinsClient.fetchBuildInfo(info, "api/json?tree=builds[number,result,timestamp]"))
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
    public JenkinsBuildListResponse getLastBuild(Long projectId, String accessToken) {
        validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode build = safelyParseJson(jenkinsClient.fetchBuildInfo(info, "lastBuild/api/json"));

        return JenkinsBuildListResponse.builder()
                .buildNumber(build.path("number").asInt())
                .buildName("MR 빌드")
                .date(DATE_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                .time(TIME_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                .status(build.path("result").asText())
                .build();
    }

    @Override
    public JenkinsBuildDetailResponse getBuildDetail(int buildNumber, Long projectId, String accessToken) {
        validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode buildInfo = safelyParseJson(jenkinsClient.fetchBuildInfo(info, buildNumber + "/api/json"));
        String consoleLog = jenkinsClient.fetchBuildLog(info, buildNumber);

        return JenkinsBuildDetailResponse.builder()
                .buildNumber(buildNumber)
                .buildName("MR 빌드")
                .overallStatus(buildInfo.path("result").asText())
                .stepList(parseConsoleLog(consoleLog))
                .build();
    }

    @Override
    public String getBuildLog(int buildNumber, Long projectId) {
        return jenkinsClient.fetchBuildLog(getJenkinsInfo(projectId), buildNumber);
    }

    @Override
    public String getBuildStatus(int buildNumber, Long projectId, String accessToken) {
        validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode build = safelyParseJson(jenkinsClient.fetchBuildInfo(info, buildNumber + "/api/json"));
        return build.path("result").asText();
    }

    @Override
    public void triggerBuild(Long projectId, String accessToken) {
        validateUserInProject(projectId, accessToken);
        jenkinsClient.triggerBuild(getJenkinsInfo(projectId));
    }

    @Override
    public List<JenkinsBuildChangeResponse> getBuildChanges(int buildNumber, Long projectId, String accessToken) {
        validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode root = safelyParseJson(jenkinsClient.fetchBuildInfo(info, buildNumber + "/api/json?tree=changeSets[items[commitId,author[fullName],msg,timestamp]]"));
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
    public List<JenkinsBuildChangeSummaryResponse> getBuildChangesWithSummary(int buildNumber, Long projectId, String accessToken) {
        validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode root = safelyParseJson(jenkinsClient.fetchBuildInfo(info, buildNumber + "/api/json?tree=changeSets[items[commitId,author[fullName],msg,timestamp,paths[file]]]"));
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
    public void logLastBuildResultToProject(Long projectId, String accessToken) {
        validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode lastBuild = safelyParseJson(
                jenkinsClient.fetchBuildInfo(info, "lastBuild/api/json")
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

    @Override
    public void issueAndSaveToken(Long projectId, String serverIp, String accessToken) {
        validateUserInProject(projectId, accessToken);
        try {
            String jenkinsUrl = "http://" + serverIp + ":9090";
            String jenkinsJobName = "seed-deployment";
            String jenkinsUsername = "seed";
            String jenkinsToken = generateTokenViaCurl(
                    jenkinsUrl,
                    jenkinsUsername,
                    "seed0206!",
                    jenkinsUsername
            );

            JenkinsInfo jenkinsInfo = JenkinsInfo.builder()
                    .projectId(projectId)
                    .baseUrl(jenkinsUrl)
                    .username(jenkinsUsername)
                    .apiToken(jenkinsToken)
                    .jobName(jenkinsJobName)
                    .build();

            jenkinsInfoRepository.save(jenkinsInfo);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JENKINS_TOKEN_SAVE_FAILED);
        }
    }

    private List<JenkinsBuildStepResponse> parseConsoleLog(String consoleLog) {
        List<JenkinsBuildStepResponse> steps = new ArrayList<>();
        String[] lines = consoleLog.split("\n");

        int stepNumber = 1;
        int echoNumber = 1;
        String currentStageName = null;
        List<JenkinsBuildEchoResponse> currentEchoes = new ArrayList<>();
        String currentStartTime = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        boolean expectingEchoContent = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            // timestamp 추출
            String timestamp = null;
            if (line.matches("^\\[\\d{2}:\\d{2}:\\d{2}]\\s.*")) {
                timestamp = line.substring(1, 9);
                line = line.substring(10);
            }

            // echo 감지: 다음 줄에 출력이 있는 경우
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

            if (line.contains("[Pipeline] echo")) {
                expectingEchoContent = true;
                continue;
            }

            // stage 시작 감지: [Pipeline] { (StageName)
            if (line.startsWith("[Pipeline] { (")) {
                int startIdx = line.indexOf('(');
                int endIdx = line.indexOf(')', startIdx);
                if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                    // 이전 단계 저장
                    if (currentStageName != null) {
                        steps.add(createStep(stepNumber++, currentStageName, currentStartTime, timestamp, currentEchoes));
                    }

                    currentStageName = line.substring(startIdx + 1, endIdx).trim();
                    currentStartTime = timestamp;
                    currentEchoes = new ArrayList<>();
                    echoNumber = 1;
                }
            }
        }

        // 마지막 스테이지 마무리
        if (currentStageName != null) {
            steps.add(createStep(stepNumber, currentStageName, currentStartTime, null, currentEchoes));
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

    private String generateTokenViaCurl(String jenkinsUrl, String username, String password, String tokenName) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // 쿠키 저장용 임시 파일 생성
            File cookieFile = File.createTempFile("jenkins_cookie", ".txt");
            String cookiePath = cookieFile.getAbsolutePath().replace("\\", "/");

            // 1. Crumb + 쿠키 요청
            List<String> crumbCommand = Arrays.asList(
                    "curl",
                    "-u", username + ":" + password,
                    "-c", cookiePath,
                    "-s",
                    jenkinsUrl + "/crumbIssuer/api/json"
            );

            Process crumbProcess = new ProcessBuilder(crumbCommand)
                    .redirectErrorStream(true).start();

            String crumbResponse = new BufferedReader(new InputStreamReader(crumbProcess.getInputStream()))
                    .lines().collect(Collectors.joining());

            if (!crumbResponse.trim().startsWith("{")) {
                throw new BusinessException(ErrorCode.JENKINS_CRUMB_REQUEST_FAILED);
            }

            JsonNode crumbJson = mapper.readTree(crumbResponse);
            String crumb = crumbJson.get("crumb").asText();
            String crumbField = crumbJson.get("crumbRequestField").asText();

            // 2️. 토큰 요청
            String tokenUrl = jenkinsUrl + "/user/" + username + "/descriptorByName/jenkins.security.ApiTokenProperty/generateNewToken";
            String tokenJsonPayload = "{\"newTokenName\":\"" + tokenName + "\"}";

            List<String> curlCommand = Arrays.asList(
                    "curl",
                    "-u", username + ":" + password,
                    "-b", cookiePath,
                    "-c", cookiePath,
                    "-s",
                    "-X", "POST",
                    tokenUrl,
                    "-H", crumbField + ":" + crumb,
                    "-H", "Content-Type: application/json",
                    "-H", "Referer: " + jenkinsUrl + "/",
                    "-d", tokenJsonPayload
            );


            Process tokenProcess = new ProcessBuilder(curlCommand)
                    .redirectErrorStream(true).start();

            String tokenResponse = new BufferedReader(new InputStreamReader(tokenProcess.getInputStream()))
                    .lines().collect(Collectors.joining());


            if (!tokenResponse.trim().startsWith("{")) {
                throw new BusinessException(ErrorCode.JENKINS_TOKEN_RESPONSE_INVALID);
            }

            JsonNode tokenJson = mapper.readTree(tokenResponse);
            String token = tokenJson.path("data").path("tokenValue").asText();

            if (token == null || token.isBlank()) {
                throw new BusinessException(ErrorCode.JENKINS_TOKEN_PARSE_FAILED);
            }

            return token;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JENKINS_TOKEN_REQUEST_FAILED);
        }

    }

    private JenkinsInfo getJenkinsInfo(Long projectId) {
        return jenkinsInfoRepository.findByProjectId(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JENKINS_INFO_NOT_FOUND));
    }

    private void validateUserInProject(Long projectId, String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        if (!userProjectRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.USER_PROJECT_NOT_FOUND);
        }
    }
}
