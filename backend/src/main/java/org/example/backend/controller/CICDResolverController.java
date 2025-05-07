package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.selfcicd.service.CICDResolverService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/self-cicd")
@RequiredArgsConstructor
@Slf4j
public class CICDResolverController {

    private final CICDResolverService cicdResolverService;

    /**
     * Jenkins 워크플로우에서 빌드 실패 시 호출할 엔드포인트
     * - Authorization 헤더에 Bearer <cicdToken>
     * - body에는 buildNumber만 전달
     */
    @PostMapping("/resolve")
    @Operation(summary = "CI/CD 셀프 힐링 트리거", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<String>> triggerSelfHealingCI(
            @RequestParam Long projectId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String accessToken
    ) {
        cicdResolverService.handleSelfHealingCI(projectId, accessToken);
        return ResponseEntity.ok(ApiResponse.success("🔧 셀프 힐링 작업이 트리거되었습니다."));
    }
}