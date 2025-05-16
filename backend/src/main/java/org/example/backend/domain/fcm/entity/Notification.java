package org.example.backend.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.fcm.enums.NotificationType;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long receiverId;

    private boolean isRead;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private String notificationTitle;

    private String notificationContent;

    private LocalDateTime createdAt;

    private Long invitationId;

    public void setRead(boolean read) { this.isRead = read; }

}
