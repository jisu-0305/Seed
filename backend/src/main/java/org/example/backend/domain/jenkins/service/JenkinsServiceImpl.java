package org.example.backend.domain.jenkins.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.auth.ProjectAccessValidator;
import org.example.backend.controller.response.jenkins.*;
import org.example.backend.domain.aireport.enums.ReportStatus;
import org.example.backend.domain.jenkins.entity.JenkinsInfo;
import org.example.backend.domain.jenkins.enums.BuildStatusType;
import org.example.backend.domain.jenkins.repository.JenkinsInfoRepository;
import org.example.backend.domain.project.entity.Project;
import org.example.backend.domain.project.entity.ProjectExecution;
import org.example.backend.domain.project.enums.BuildStatus;
import org.example.backend.domain.project.enums.ExecutionType;
import org.example.backend.domain.project.enums.ServerStatus;
import org.example.backend.domain.project.repository.ProjectExecutionRepository;
import org.example.backend.domain.project.repository.ProjectRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
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
@Slf4j
public class JenkinsServiceImpl implements JenkinsService {

    private final JenkinsClient jenkinsClient;
    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final ProjectExecutionRepository projectExecutionRepository;
    private final ProjectAccessValidator projectAccessValidator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy. M. d. a hh:mm:ss")
            .withZone(ZoneId.systemDefault())
            .withLocale(Locale.KOREA);

    @Override
    public JenkinsBuildPageResponse getBuildList(Long projectId, int start, int limit, String accessToken) {
        projectAccessValidator.validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);

        // Jenkins는 start, limit 안 먹힘. 전부 받아와서 자바에서 슬라이싱
        JsonNode buildsNode = safelyParseJson(
                jenkinsClient.fetchBuildInfo(info, "api/json?tree=builds[number,result,timestamp]")
        ).path("builds");

        List<JenkinsBuildListResponse> allBuilds = new ArrayList<>();
        for (JsonNode build : buildsNode) {
            allBuilds.add(JenkinsBuildListResponse.builder()
                    .buildNumber(build.path("number").asInt())
                    .buildName("MR 빌드")
                    .date(DATE_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                    .time(TIME_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                    .status(build.path("result").asText())
                    .build());
        }

        // 커서 방식 슬라이싱
        int end = Math.min(start + limit, allBuilds.size());
        List<JenkinsBuildListResponse> sliced = start < allBuilds.size() ? allBuilds.subList(start, end) : List.of();
        boolean hasNext = end < allBuilds.size();
        Integer nextStart = hasNext ? end : null;

        return JenkinsBuildPageResponse.builder()
                .builds(sliced)
                .hasNext(hasNext)
                .nextStart(nextStart)
                .build();
    }

    @Override
    public int getLastBuildNumberWithOutLogin(Long projectId){
        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode build = safelyParseJson(jenkinsClient.fetchBuildInfo(info, "lastBuild/api/json"));

        return JenkinsBuildListResponse.builder()
                .buildNumber(build.path("number").asInt())
                .buildName("MR 빌드")
                .date(DATE_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                .time(TIME_FORMATTER.format(Instant.ofEpochMilli(build.path("timestamp").asLong())))
                .status(build.path("result").asText())
                .build().getBuildNumber();
    }

    @Override
    public JenkinsBuildListResponse getLastBuild(Long projectId, String accessToken) {
        projectAccessValidator.validateUserInProject(projectId, accessToken);
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
        projectAccessValidator.validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);

        String wfapiJson = jenkinsClient.fetchBuildInfo(info, buildNumber + "/wfapi/describe");
        String consoleLog = jenkinsClient.fetchBuildLog(info, buildNumber);

        List<JenkinsBuildStepResponse> steps = mergeStageStatusWithEchoes(wfapiJson, consoleLog);

        JsonNode buildInfo = safelyParseJson(jenkinsClient.fetchBuildInfo(info, buildNumber + "/api/json"));

        String rawStatus = buildInfo.path("result").asText();
        BuildStatusType status = BuildStatusType.from(rawStatus);

        return JenkinsBuildDetailResponse.builder()
                .buildNumber(buildNumber)
                .buildName("MR 빌드")
                .overallStatus(status.name())
                .stepList(steps)
                .build();
    }

    @Override
    public String getBuildLogWithOutLogin(int buildNumber, Long projectId){
        return jenkinsClient.fetchBuildLog(getJenkinsInfo(projectId), buildNumber);
    }

    @Override
    public String getBuildLog(int buildNumber, Long projectId, String accessToken) {
        projectAccessValidator.validateUserInProject(projectId, accessToken);
        return jenkinsClient.fetchBuildLog(getJenkinsInfo(projectId), buildNumber);
    }

    @Override
    public String getBuildStatusWithOutLogin(int buildNumber, Long projectId) {
        try {
            JenkinsInfo info = getJenkinsInfo(projectId);
            String json = jenkinsClient.fetchBuildInfo(info, buildNumber + "/api/json");

            if (json == null || json.isBlank()) return "BUILDING";

            JsonNode build = new ObjectMapper().readTree(json);
            JsonNode resultNode = build.path("result");

            if (resultNode.isNull() || "null".equalsIgnoreCase(resultNode.asText())) {
                return "BUILDING";
            }

            return resultNode.asText();
        } catch (Exception e) {
            return "BUILDING";
        }
    }

    @Override
    public String getBuildStatus(int buildNumber, Long projectId, String accessToken) {
        projectAccessValidator.validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode build = safelyParseJson(jenkinsClient.fetchBuildInfo(info, buildNumber + "/api/json"));
        return build.path("result").asText();
    }

    @Override
    public void triggerBuildWithOutLogin(Long projectId, String branchName, String originalBranchName) {
        jenkinsClient.triggerBuildWithoutLogin(getJenkinsInfo(projectId), branchName, originalBranchName);
    }

    @Override
    @Transactional
    public void triggerBuild(Long projectId, String accessToken, String branchName) {
        projectAccessValidator.validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);

        jenkinsClient.triggerBuild(info, branchName);

        // 마지막 빌드 완료 대기 (최대 60초)
        JsonNode lastBuild = waitUntilBuildCompleted(info, 60);

        int buildNumber = lastBuild.path("number").asInt();
        BuildStatus status = BuildStatus.valueOf(lastBuild.path("result").asText("UNKNOWN"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_STATUS_NOT_FOUND));
        project.updateBuildStatus(status);

        projectExecutionRepository.save(ProjectExecution.builder()
                .projectId(projectId)
                .executionType(ExecutionType.BUILD)
                .projectExecutionTitle("#" + buildNumber + " MR 빌드")
                .executionStatus(status)
                .buildNumber(String.valueOf(buildNumber))
                .createdAt(LocalDate.now())
                .build());

        log.info("✅ Jenkins 빌드 결과 저장 완료: 프로젝트={}, 빌드번호=#{}", projectId, buildNumber);
    }



    @Override
    public List<JenkinsBuildChangeResponse> getBuildChanges(int buildNumber, Long projectId, String accessToken) {
        projectAccessValidator.validateUserInProject(projectId, accessToken);
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
        projectAccessValidator.validateUserInProject(projectId, accessToken);
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
    public void logLastBuildResultToProject(Long projectId) {

        JenkinsInfo info = getJenkinsInfo(projectId);
        JsonNode lastBuild = safelyParseJson(
                jenkinsClient.fetchBuildInfo(info, "lastBuild/api/json")
        );

        int buildNumber = lastBuild.path("number").asInt();
        BuildStatus status = BuildStatus.valueOf(lastBuild.path("result").asText());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_STATUS_NOT_FOUND));

        project.updateBuildStatus(status);

        projectExecutionRepository.save(ProjectExecution.builder()
                .projectId(projectId)
                .executionType(ExecutionType.BUILD)
                .projectExecutionTitle("#" + buildNumber + " MR 빌드")
                .executionStatus(status)
                .buildNumber(String.valueOf(buildNumber))
                .createdAt(LocalDate.now())
                .createdTime(LocalTime.now())
                .build());
    }

    @Override
    public void issueAndSaveToken(Long projectId, String serverIp, String accessToken) {
        projectAccessValidator.validateUserInProject(projectId, accessToken);
        try {
            String jenkinsUrl = "http://" + serverIp + ":9090";
            String jenkinsJobName = "drum-dummy1";
            String jenkinsUsername = "admin";
            String jenkinsToken = generateTokenViaCurl(
                    jenkinsUrl,
                    jenkinsUsername,
                    "pwd123",
                    jenkinsUsername
            );

            Optional<JenkinsInfo> optional = jenkinsInfoRepository.findByProjectId(projectId);

            JenkinsInfo jenkinsInfo = optional.map(existing ->
                    existing.toBuilder()
                            .baseUrl(jenkinsUrl)
                            .username(jenkinsUsername)
                            .apiToken(jenkinsToken)
                            .jobName(jenkinsJobName)
                            .build()
            ).orElseGet(() ->
                    JenkinsInfo.builder()
                            .projectId(projectId)
                            .baseUrl(jenkinsUrl)
                            .username(jenkinsUsername)
                            .apiToken(jenkinsToken)
                            .jobName(jenkinsJobName)
                            .build()
            );

            jenkinsInfoRepository.save(jenkinsInfo);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JENKINS_TOKEN_SAVE_FAILED);
        }
    }

    @Override
    public String getStepLogById(Long projectId, int buildNumber, String stepNumber, String accessToken) {
        projectAccessValidator.validateUserInProject(projectId, accessToken);
        JenkinsInfo info = getJenkinsInfo(projectId);

        // 전체 콘솔 로그 조회
        String consoleLog = jenkinsClient.fetchBuildLog(info, buildNumber);
        String[] lines = consoleLog.split("\n");

        int targetStepIndex;
        try {
            targetStepIndex = Integer.parseInt(stepNumber);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_STEP_ID);
        }

        int currentStepIndex = 0;
        String currentStage = null;
        List<String> collected = new ArrayList<>();
        boolean insideTargetStage = false;

        for (String line : lines) {
            if (line.contains("[Pipeline] { (")) {
                currentStepIndex++;
                String stageName = line.substring(line.indexOf('(') + 1, line.indexOf(')'));

                if (currentStepIndex == targetStepIndex) {
                    insideTargetStage = true;
                    currentStage = stageName;
                    collected.add("=== Stage: " + stageName + " ===");
                } else {
                    insideTargetStage = false;
                }
                continue;
            }

            if (insideTargetStage) {
                collected.add(line);
            }
        }

        if (collected.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_STEP_ID);
        }

        return String.join("\n", collected);
    }

    private List<JenkinsBuildStepResponse> mergeStageStatusWithEchoes(String wfapiJson, String consoleLog) {
        JsonNode wfapi = safelyParseJson(wfapiJson);
        JsonNode stages = wfapi.path("stages");

        Map<String, String> stageStatusMap = new LinkedHashMap<>();
        for (JsonNode stage : stages) {
            String name = stage.path("name").asText();
            String status = stage.path("status").asText();
            stageStatusMap.put(name, status);
        }

        Map<String, List<String>> echoMap = extractEchoesFromConsole(consoleLog);

        List<JenkinsBuildStepResponse> stepList = new ArrayList<>();
        int stepNumber = 1;

        for (Map.Entry<String, String> entry : stageStatusMap.entrySet()) {
            String stageName = entry.getKey();
            String status = entry.getValue();

            List<String> echoes = echoMap.getOrDefault(stageName, Collections.emptyList());
            List<JenkinsBuildEchoResponse> echoDtos = new ArrayList<>();
            int echoNumber = 1;
            for (String echo : echoes) {
                echoDtos.add(JenkinsBuildEchoResponse.builder()
                        .echoNumber(echoNumber++)
                        .echoContent(echo)
                        .duration("-")
                        .build());
            }

            stepList.add(JenkinsBuildStepResponse.builder()
                    .stepNumber(stepNumber++)
                    .stepName(stageName)
                    .status(status)
                    .duration("-")
                    .echoList(echoDtos)
                    .build());
        }

        return stepList;
    }

    private Map<String, List<String>> extractEchoesFromConsole(String consoleLog) {
        Map<String, List<String>> stageEchoMap = new LinkedHashMap<>();
        String[] lines = consoleLog.split("\n");

        String currentStage = null;
        List<String> currentEchoes = new ArrayList<>();
        boolean expectingEcho = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.startsWith("[Pipeline] { (")) {
                if (currentStage != null) {
                    stageEchoMap.put(currentStage, new ArrayList<>(currentEchoes));
                }
                int startIdx = line.indexOf('(');
                int endIdx = line.indexOf(')', startIdx);
                if (startIdx != -1 && endIdx != -1) {
                    currentStage = line.substring(startIdx + 1, endIdx).trim();
                    currentEchoes.clear();
                }
                continue;
            }

            if (line.contains("[Pipeline] echo")) {
                expectingEcho = true;
                continue;
            }

            if (expectingEcho && !line.startsWith("[Pipeline]") && !line.isBlank()) {
                currentEchoes.add(line);
                expectingEcho = false;
            }
        }

        if (currentStage != null) {
            stageEchoMap.put(currentStage, currentEchoes);
        }

        return stageEchoMap;
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

    @Override
    public ReportStatus waitUntilBuildFinishes(int newBuildNumber, Long projectId) {
        int maxTries = 60; //5초간 60번 총 5분진행
        int intervalMillis = 5000;

        for (int i = 0; i < maxTries; i++) {
            String status = getBuildStatusWithOutLogin(newBuildNumber, projectId); // 🔥 핵심: 이게 "BUILDING"인지 체크
            log.debug(">>>>>>>> Jenkins build #{} 상태 = {}", newBuildNumber, status);

            if (!"BUILDING".equalsIgnoreCase(status)) {
                log.debug("✅ Jenkins 빌드 완료: status = {}", status);
                return ReportStatus.fromJenkinsStatus(status); // SUCCESS / FAIL 반환
            }

            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
            }
        }
        return ReportStatus.FAIL;
    }

    private JenkinsInfo getJenkinsInfo(Long projectId) {
        return jenkinsInfoRepository.findByProjectId(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JENKINS_INFO_NOT_FOUND));
    }

    private JsonNode waitUntilBuildCompleted(JenkinsInfo info, int timeoutSeconds) {
        int elapsed = 0;
        int interval = 2;

        while (elapsed < timeoutSeconds) {
            JsonNode lastBuild = safelyParseJson(jenkinsClient.fetchBuildInfo(info, "lastBuild/api/json"));
            String result = lastBuild.path("result").asText();

            if (!result.isEmpty() && !result.equals("null")) {
                return lastBuild;
            }

            try {
                Thread.sleep(interval * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            elapsed += interval;
        }

        throw new BusinessException(ErrorCode.BUSINESS_ERROR);
    }
}
