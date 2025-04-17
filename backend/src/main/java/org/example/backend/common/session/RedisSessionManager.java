package org.example.backend.common.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.jwt.JwtTokenProvider;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.enums.ProviderType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSessionManager {

    private static final String SESSION_PREFIX = "session:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public void saveSession(String accessToken, User user, String oauthUserId) {
        String sessionKey = SESSION_PREFIX + user.getId();

        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(accessToken);
        long ttlSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;

        SessionInfoDto sessionInfoDto = SessionInfoDto.builder()
                .oauthId(oauthUserId)
                .providerType(ProviderType.GITLAB)
                .userId(user.getId())
                .build();

        redisTemplate.opsForValue().set(sessionKey, sessionInfoDto, ttlSeconds, TimeUnit.SECONDS);

        log.info("Session stored for userId: {} with TTL: {} seconds", user.getId(), ttlSeconds);
    }

    public SessionInfoDto getSession(String jwtToken) {
        String userId = jwtTokenProvider.getSubjectFromToken(jwtToken);
        String sessionKey = SESSION_PREFIX + userId;
        Object sessionObj = redisTemplate.opsForValue().get(sessionKey);
        if (sessionObj instanceof SessionInfoDto sessionInfoDto) {
            log.info("Session retrieved for userId: {}", userId);
            return sessionInfoDto;
        }
        log.warn("No session found for userId: {}", userId);
        return null;
    }

    public void deleteSession(String jwtToken) {
        String userId = jwtTokenProvider.getSubjectFromToken(jwtToken);
        String sessionKey = SESSION_PREFIX + userId;
        redisTemplate.delete(sessionKey);
        log.info("Session deleted for userId: {}", userId);
    }
}