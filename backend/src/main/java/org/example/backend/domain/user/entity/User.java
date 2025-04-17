package org.example.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.user.enums.ProviderType;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    private String oauthUserId;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime createdAt;
}
