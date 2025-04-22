package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fcm.service.FcmTokenService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm-tokens")
@RequiredArgsConstructor
@Tag(name = "FCM", description = "FCM 토큰 관리 API")
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping
    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록하거나 갱신합니다.")
    public ResponseEntity<ApiResponse<Void>> registerToken(@RequestParam Long userId, @RequestParam String token) {
        fcmTokenService.register(userId, token);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping
    @Operation(summary = "FCM 토큰 삭제", description = "로그아웃 시 사용자의 FCM 토큰을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteToken(@RequestParam String token) {
        fcmTokenService.deleteByToken(token);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
