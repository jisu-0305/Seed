package org.example.backend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.response.jenkins.JenkinsBuildChangeResponse;
import org.example.backend.controller.response.jenkins.JenkinsBuildChangeSummaryResponse;
import org.example.backend.controller.response.jenkins.JenkinsBuildDetailResponse;
import org.example.backend.controller.response.jenkins.JenkinsBuildListResponse;
import org.example.backend.domain.jenkins.service.JenkinsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Jenkins API", description = "Jenkins 빌드 정보 관련 API")
@RestController
@RequestMapping("/api/jenkins")
@RequiredArgsConstructor
public class JenkinsController {

    private final JenkinsService jenkinsService;

    @Operation(summary = "빌드 목록 조회", description = "전체 빌드 기록 목록을 조회합니다.")
    @GetMapping("/builds")
    public List<JenkinsBuildListResponse> getBuildList() {
        return jenkinsService.getBuildList();
    }

    @Operation(summary = "최근 빌드 조회", description = "가장 최근 빌드 기록을 조회합니다.")
    @GetMapping("/builds/last")
    public JenkinsBuildListResponse getLastBuild() {
        return jenkinsService.getLastBuild();
    }

    @Operation(summary = "빌드 상세 조회", description = "특정 빌드 번호에 대한 작업(step) 리스트를 조회합니다.")
    @GetMapping("/builds/{buildNumber}")
    public JenkinsBuildDetailResponse getBuildDetail(@PathVariable int buildNumber) {
        return jenkinsService.getBuildDetail(buildNumber);
    }

    @Operation(summary = "빌드 콘솔 로그 조회", description = "특정 빌드 번호의 콘솔 로그를 조회합니다.")
    @GetMapping("/builds/{buildNumber}/log")
    public String getBuildLog(@PathVariable int buildNumber) {
        return jenkinsService.getBuildLog(buildNumber);
    }

    @Operation(summary = "빌드 상태 조회", description = "특정 빌드 번호의 SUCCESS/FAILURE 상태를 조회합니다.")
    @GetMapping("/builds/{buildNumber}/status")
    public String getBuildStatus(@PathVariable int buildNumber) {
        return jenkinsService.getBuildStatus(buildNumber);
    }

    @Operation(summary = "빌드 수동 트리거", description = "Jenkins Job을 수동으로 트리거(빌드 시작)합니다.")
    @PostMapping("/trigger")
    public void triggerBuild() {
        jenkinsService.triggerBuild();
    }

    @Operation(summary = "빌드 커밋 변경사항 조회", description = "특정 빌드 번호의 커밋 변경 내역을 조회합니다.")
    @GetMapping("/builds/{buildNumber}/changes")
    public List<JenkinsBuildChangeResponse> getBuildChanges(@PathVariable int buildNumber) {
        return jenkinsService.getBuildChanges(buildNumber);
    }

    @Operation(summary = "빌드 커밋 상세 조회", description = "특정 빌드 번호의 커밋 및 수정 파일 요약 정보를 조회합니다.")
    @GetMapping("/builds/{buildNumber}/changes/summary")
    public List<JenkinsBuildChangeSummaryResponse> getBuildChangeSummary(@PathVariable int buildNumber) {
        return jenkinsService.getBuildChangesWithSummary(buildNumber);
    }
}