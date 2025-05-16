package org.example.backend.domain.userproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.fcm.entity.Notification;
import org.example.backend.domain.userproject.enums.InvitationStateType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invitation")
public class Invitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private Long senderId;

    private Long receiverId;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private InvitationStateType state;

    @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    public static Invitation create(Long projectId, Long senderId, Long receiverId) {
        return Invitation.builder()
                .projectId(projectId)
                .senderId(senderId)
                .receiverId(receiverId)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .state(InvitationStateType.PENDING)
                .build();
    }

    public void accept() {
        this.state = InvitationStateType.ACCEPTED;
    }

}
