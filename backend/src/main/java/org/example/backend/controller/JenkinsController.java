package org.example.backend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.response.jenkins.*;
import org.example.backend.domain.jenkins.service.JenkinsService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Jenkins API", description = "Jenkins 빌드 정보 관련 API")
@RestController
@RequestMapping("/api/jenkins")
@RequiredArgsConstructor
public class JenkinsController {

    private final JenkinsService jenkinsService;

    @GetMapping("/{projectId}/builds")
    @Operation(summary = "빌드 목록 조회", description = "전체 빌드 기록 목록을 조회합니다. (커서 방식)")
    public JenkinsBuildPageResponse getBuildList(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "100") int limit,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        return jenkinsService.getBuildList(projectId, start, limit, accessToken);
    }


    @Operation(summary = "최근 빌드 조회", description = "가장 최근 빌드 기록을 조회합니다.")
    @GetMapping("/{projectId}/builds/last")
    public JenkinsBuildListResponse getLastBuild(@PathVariable Long projectId,
                                                 @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        return jenkinsService.getLastBuild(projectId, accessToken);
    }

    @Operation(summary = "빌드 상세 조회", description = "특정 빌드 번호에 대한 작업(step) 리스트를 조회합니다.")
    @GetMapping("/{projectId}/builds/{buildNumber}")
    public JenkinsBuildDetailResponse getBuildDetail(@PathVariable Long projectId,
                                                     @PathVariable int buildNumber,
                                                     @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        return jenkinsService.getBuildDetail(buildNumber, projectId, accessToken);
    }

    @Operation(summary = "빌드 콘솔 로그 조회", description = "특정 빌드 번호의 콘솔 로그를 조회합니다.")
    @GetMapping("/{projectId}/builds/{buildNumber}/log")
    public String getBuildLog(@PathVariable Long projectId,
                              @PathVariable int buildNumber,
                              @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        return jenkinsService.getBuildLog(buildNumber, projectId, accessToken);
    }

    @Operation(summary = "빌드 상태 조회", description = "특정 빌드 번호의 SUCCESS/FAILURE 상태를 조회합니다.")
    @GetMapping("/{projectId}/builds/{buildNumber}/status")
    public String getBuildStatus(@PathVariable Long projectId,
                                 @PathVariable int buildNumber,
                                 @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        return jenkinsService.getBuildStatus(buildNumber, projectId, accessToken);
    }

    @Operation(summary = "빌드 수동 트리거", description = "Jenkins Job을 수동으로 트리거(빌드 시작)합니다.")
    @PostMapping("/{projectId}/trigger")
    public void triggerBuild(@PathVariable Long projectId,
                             @RequestParam String branchName,
                             @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        jenkinsService.triggerBuild(projectId, accessToken, branchName);
    }


    @Operation(summary = "빌드 커밋 변경사항 조회", description = "특정 빌드 번호의 커밋 변경 내역을 조회합니다.")
    @GetMapping("/{projectId}/builds/{buildNumber}/changes")
    public List<JenkinsBuildChangeResponse> getBuildChanges(@PathVariable Long projectId,
                                                            @PathVariable int buildNumber,
                                                            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        return jenkinsService.getBuildChanges(buildNumber, projectId, accessToken);
    }

    @Operation(summary = "빌드 커밋 상세 조회", description = "특정 빌드 번호의 커밋 및 수정 파일 요약 정보를 조회합니다.")
    @GetMapping("/{projectId}/builds/{buildNumber}/changes/summary")
    public List<JenkinsBuildChangeSummaryResponse> getBuildChangeSummary(@PathVariable Long projectId,
                                                                         @PathVariable int buildNumber,
                                                                         @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        return jenkinsService.getBuildChangesWithSummary(buildNumber, projectId, accessToken);
    }

    @PostMapping("/issue-token")
    public ResponseEntity<String> testTokenIssue(@RequestParam Long projectId, @RequestParam String serverIp,@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        jenkinsService.issueAndSaveToken(projectId, serverIp, accessToken);
        return ResponseEntity.ok("토큰 발급 및 저장 완료");
    }

    @Operation(summary = "단계별 로그 조회", description = "빌드 번호와 단계명(stepId)을 이용해 해당 단계의 콘솔 로그를 조회합니다.")
    @GetMapping("/{projectId}/builds/{buildNumber}/{stepNumber}")
    public ResponseEntity<String> getStepLogById(@PathVariable Long projectId,
                                                 @PathVariable int buildNumber,
                                                 @PathVariable String stepNumber,
                                                 @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken) {
        String log = jenkinsService.getStepLogById(projectId, buildNumber, stepNumber, accessToken);
        return ResponseEntity.ok(log);
    }

    @PostMapping("/{projectId}/log-last-build")
    public ResponseEntity<ApiResponse<Void>> logLastBuildResultToProject(
            @PathVariable Long projectId
    ) {
        jenkinsService.logLastBuildResultToProject(projectId);
        return ResponseEntity.ok(ApiResponse.success());
    }


}
