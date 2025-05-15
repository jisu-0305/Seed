package org.example.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.gitlab.dto.GitlabCompareDiff;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.domain.gitlab.service.GitlabService;
import org.example.backend.domain.selfcicd.service.CICDResolverService;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.util.aiapi.dto.patchfile.PatchFileRequest;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileInnerResponse;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileRequest;
import org.example.backend.util.backoffice.SimulationRequestDto;
import org.example.backend.util.aiapi.AIApiClient;
import org.example.backend.util.aiapi.dto.resolvefile.ResolveErrorResponse;
import org.example.backend.util.aiapi.dto.suspectapp.InferAppRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/self-cicd")
@RequiredArgsConstructor
@Slf4j
public class CICDResolverController {

    private final CICDResolverService cicdResolverService;
    private final AIApiClient fastAIClient;
    private final GitlabService gitlabService;
    private final ObjectMapper objectMapper;

    /**
     * Jenkins 워크플로우에서 빌드 실패 시 호출할 엔드포인트
     * - Authorization 헤더에 Bearer <cicdToken>
     * - body에는 buildNumber만 전달
     */
    @PostMapping("/resolve")
    @Operation(summary = "CI/CD 셀프 힐링 트리거")
    public ResponseEntity<ApiResponse<String>> triggerSelfHealingCI(
            @RequestParam Long projectId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String accessToken
    ) {
        cicdResolverService.handleSelfHealingCI(projectId, accessToken);
        return ResponseEntity.ok(ApiResponse.success("🔧 셀프 힐링 작업이 트리거되었습니다."));
    }

    @PostMapping("/resolve/test")
    @Operation(summary = "CI/CD 셀프 힐링 트리거")
    public ResponseEntity<ApiResponse<String>> triggerSelfHealing(
            @RequestParam Long projectId,
            @RequestParam String personalAccessToken,
            @RequestParam String failType // BUILD, RUNTIME
    ) {

        return ResponseEntity.ok(ApiResponse.success("🔧 셀프 힐링 작업이 트리거되었습니다." + projectId + " " + personalAccessToken + " " + failType));
    }

    // AI 통합 테스트용 controller 추후 삭제 필요
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<List<PatchedFile>>> simulateSelfHealing(
            @RequestBody SimulationRequestDto request
    ) {
        String accessToken = request.getAccessToken();
        Long projectId = request.getProjectId();
        String jenkinsErrorLog = request.getJenkinsErrorLog();

        List<GitlabCompareDiff> gitDiff = request.getGitDiff();
        Map<String, String> appLogs = request.getAppLogs();
        List<Map<String, String>> allTrees = request.getTree();
        List<String> appNames = request.getApplicationNames();

        Map<String, String> appToFolderMap = Map.of(
                "spring", "backend",
                "react", "frontend"
        );

        // 3. 앱 추론
        InferAppRequest inferRequest = InferAppRequest.builder()
                .gitDiff(gitDiff)
                .jenkinsLog(jenkinsErrorLog)
                .applicationNames(appNames)
                .build();
        log.debug(">>>>>>>>>>>>>시작");
        List<String> suspectedApps = fastAIClient.requestInferApplications(inferRequest);
        log.debug(">>>>>>>>>>>>>의심 app 찾기"+suspectedApps.toString());

        // 4. 트리 매핑
        Map<String, List<GitlabTree>> appTrees = new HashMap<>();
        for (String appName : suspectedApps) {
            String folder = appToFolderMap.getOrDefault(appName, appName); // ✅ 수정
            List<GitlabTree> parsedTree = allTrees.stream()
                    .filter(node -> node.get("path").startsWith(folder + "/"))
                    .map(node -> objectMapper.convertValue(node, GitlabTree.class))
                    .collect(Collectors.toList());
            appTrees.put(appName, parsedTree);
        }

        log.debug(">>>>>>>>>>>트리 맵 찾기");

        List<PatchedFile> patchedFiles = new ArrayList<>();

        for (String appName : suspectedApps) {
            String appLog = appLogs.get(appName);
            List<GitlabTree> tree = appTrees.get(appName);

            Map<String, Object> diffRawPayload = new HashMap<>();
            diffRawPayload.put("commit", Map.of(
                    "title", "auto-generated commit",
                    "message", "generated by simulateSelfHealing()"
            ));
            diffRawPayload.put("diffs", gitDiff);

            String diffJson;
            String treeJson;
            try {
                diffJson = objectMapper.writeValueAsString(diffRawPayload);
                treeJson = objectMapper.writeValueAsString(tree);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.AI_INFER_REQUEST_FAILED);
            }

            SuspectFileRequest suspectRequest = SuspectFileRequest.builder()
                    .diffRaw(diffJson)
                    .tree(treeJson)
                    .log(appLog)
                    .build();

            SuspectFileInnerResponse suspectFileInnerResponse = fastAIClient.requestSuspectFiles(suspectRequest).getResponse();
            log.debug(">>>>>>>>>>>>>의심파일 찾기"+suspectFileInnerResponse.getSuspectFiles().toString());

            List<Map<String, String>> filesRaw = new ArrayList<>();
            for (var f : suspectFileInnerResponse.getSuspectFiles()) {
                String path = f.getPath();
                String code = gitlabService.getRawFileContent(accessToken, projectId, path, "master");
                filesRaw.add(Map.of("path", path, "code", code));
                log.debug(">>>>>>>>>>>>>깃렙 api 파일 path: "+path+", 소스코드: "+code);
            }

            String rawJson;
            try {
                rawJson = objectMapper.writeValueAsString(filesRaw);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.AI_RESOLVE_REQUEST_FAILED);
            }

            ResolveErrorResponse resolveDto = fastAIClient.requestResolveError(suspectFileInnerResponse, rawJson);
            log.debug(">>>>>>>>>>>>>해결책 요약: "+resolveDto.toString());

            for (var fix : resolveDto.getResponse().getFileFixes()) {
                String path = fix.getPath();
                String instruction = fix.getInstruction();
                String code = filesRaw.stream()
                        .filter(f -> f.get("path").equals(path))
                        .findFirst()
                        .map(f -> f.get("code"))
                        .orElse("");

                PatchFileRequest patchFileRequest = PatchFileRequest.builder()
                        .path(path)
                        .originalCode(code)
                        .instruction(instruction)
                        .build();

                PatchedFile patchedFile = fastAIClient.requestPatchFile(patchFileRequest);
                log.debug(">>>>>>>>>>>>>변경된 파일 내용: "+patchedFile.getPatchedCode().toString());
                patchedFiles.add(patchedFile);
            }
        }

        return ResponseEntity.ok(ApiResponse.success(patchedFiles));
    }
}
