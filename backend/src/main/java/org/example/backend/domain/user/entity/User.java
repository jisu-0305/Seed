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

    private String oauthClientId;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    private String gitlabPersonalAccessToken;

    private String gitlabAccessToken;

    private String gitlabRefreshToken;

    private String userName;

    private String userIdentifyId;

    private String profileImageUrl;

    private LocalDateTime createdAt;

    private boolean hasGitlabPersonalAccessToken;
}
