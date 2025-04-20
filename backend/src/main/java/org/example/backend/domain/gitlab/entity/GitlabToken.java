package org.example.backend.domain.gitlab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.common.util.CryptoUtil;
import org.example.backend.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gitlab_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uk_gitlab_token_user", columnNames = "user_id"))
public class GitlabToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "encrypted_token", nullable = false, length = 512)
    private String encryptedToken;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static GitlabToken of(User user, String encryptedToken) {
        return GitlabToken.builder()
                .user(user)
                .encryptedToken(encryptedToken)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void changeToken(String newEncryptedToken) {
        this.encryptedToken = newEncryptedToken;
        this.updatedAt = LocalDateTime.now();
    }

    @Transient       // DB 컬럼 아님
    public String getDecryptedToken(CryptoUtil cryptoUtil) {
        return cryptoUtil.decrypt(this.encryptedToken);
    }
}
