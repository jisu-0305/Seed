package org.example.backend.domain.fcm.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fcm.entity.FcmToken;
import org.example.backend.domain.fcm.repository.FcmTokenRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmTokenServiceImpl implements FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    @Override
    public void register(Long userId, String token) {
        fcmTokenRepository.findByUserId(userId).ifPresentOrElse(
                existing -> {
                    existing.updateToken(token);
                    fcmTokenRepository.save(existing);
                },
                () -> fcmTokenRepository.save(FcmToken.of(userId, token))
        );
    }

    @Override
    public void deleteByToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }
}
