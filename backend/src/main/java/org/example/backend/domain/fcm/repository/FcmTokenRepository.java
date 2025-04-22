package org.example.backend.domain.fcm.repository;

import org.example.backend.domain.fcm.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUserId(Long userId);
    List<FcmToken> findByUserIdIn(List<Long> userIdList);
    void deleteByToken(String token);
}
