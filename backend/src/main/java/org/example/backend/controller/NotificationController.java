package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.domain.fcm.dto.NotificationDto;
import org.example.backend.domain.fcm.entity.Notification;
import org.example.backend.domain.fcm.mapper.NotificationMapper;
import org.example.backend.domain.fcm.service.NotificationService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final RedisSessionManager redisSessionManager;

    @GetMapping
    @Operation(summary = "전체 알림 페이징 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getAllNotifications(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> result = notificationService.getAllNotifications(accessToken, pageable);
        return ResponseEntity.ok(ApiResponse.success(NotificationMapper.toDtoPage(result)));
    }

    @GetMapping("/unread")
    @Operation(summary = "읽지 않은 알림 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnreadNotifications(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        List<NotificationDto> dtoList = notificationService.getUnreadNotifications(accessToken);
        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken
    ) {
        notificationService.markAsRead(id, accessToken);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
