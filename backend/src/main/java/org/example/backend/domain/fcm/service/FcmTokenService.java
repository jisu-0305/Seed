package org.example.backend.domain.fcm.service;

public interface FcmTokenService {
    void register(Long userId, String token);
    void deleteByToken(String token);
}
