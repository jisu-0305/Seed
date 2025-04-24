package org.example.backend.common.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.jwt.JwtTokenProvider;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.enums.ProviderType;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
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

        log.debug("Session stored for userId: {} with TTL: {} seconds", user.getId(), ttlSeconds);
    }

    public SessionInfoDto getSession(String jwtToken) {
        // 1) Bearer 접두사 제거
        String token = jwtToken.substring(7).trim();

        // 2) JWT에서 userId 추출
        String userId = jwtTokenProvider.getSubjectFromToken(token);
        String sessionKey = SESSION_PREFIX + userId;

        // 3) Redis에서 세션 객체 조회
        Object sessionObj = redisTemplate.opsForValue().get(sessionKey);

        // 4) 타입 검사 + 반환
        if (sessionObj instanceof SessionInfoDto sessionInfo) {
            log.debug("Session retrieved for userId: {}", userId);
            return sessionInfo;
        }

        // 5) 없거나 타입이 맞지 않으면 인증 실패
        log.warn("No valid session found for userId: {}", userId);
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    public void deleteSession(String jwtToken) {
        String token = jwtToken.substring(7).trim();
        String userId = jwtTokenProvider.getSubjectFromToken(token);
        String sessionKey = SESSION_PREFIX + userId;
        redisTemplate.delete(sessionKey);
        log.debug("Session deleted for userId: {}", userId);
    }
}