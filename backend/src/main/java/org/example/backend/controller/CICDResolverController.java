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
import org.example.backend.domain.selfcicd.enums.FailType;
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

    @PostMapping("/resolve")
    @Operation(summary = "CI/CD 셀프 힐링 트리거")
    public ResponseEntity<ApiResponse<String>> triggerSelfHealingCI(
            @RequestParam Long projectId,
            @RequestParam String personalAccessToken,
            @RequestParam String failType
    ) {
        cicdResolverService.handleSelfHealingCI(projectId, personalAccessToken, FailType.from(failType));
        String message = "셀프 힐링 작업이 트리거되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/resolve/test")
    @Operation(summary = "CI/CD 셀프 힐링 트리거")
    public ResponseEntity<ApiResponse<String>> triggerSelfHealing(
            @RequestParam Long projectId,
            @RequestParam String personalAccessToken,
            @RequestParam String failType // BUILD, RUNTIME
    ) {

        String message = String.format(
                "🔧 셀프 힐링 작업이 트리거되었습니다. [projectId=%d, personalAccessToken=%s, failType=%s]",
                projectId, personalAccessToken, failType
        );

        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
