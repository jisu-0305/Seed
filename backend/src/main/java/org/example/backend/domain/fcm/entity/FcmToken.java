package org.example.backend.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String token;

    public static FcmToken of(Long userId, String token) {
        return FcmToken.builder()
                .userId(userId)
                .token(token)
                .build();
    }

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
