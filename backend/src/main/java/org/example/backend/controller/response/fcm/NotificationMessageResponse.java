package org.example.backend.controller.response.fcm;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessageResponse {
    private Long id;
    private Long userId;
    private String title;
    private String body;
    private Long invitationId;
    private LocalDateTime createdAt;
}
