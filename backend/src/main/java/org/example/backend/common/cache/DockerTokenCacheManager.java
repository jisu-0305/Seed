package org.example.backend.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.cache.dto.TokenResponse;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerTokenCacheManager {

    @Value("${docker.token.prefix}")
    private String tokenPrefix;
    private final RedisTemplate<String, String> redisTemplate;
    private final WebClient dockerAuthWebClient;

    public String getAnonymousToken(String namespace, String repo) {
        String key = tokenPrefix + namespace + "/" + repo;

        // 1) 레디스 캐시 확인
        String cachedToken = redisTemplate.opsForValue().get(key);
        if (cachedToken != null) {
            log.debug("Found cached Docker token for {}/{}", namespace, repo);
            return cachedToken;
        }

        // 2) 없으면 인증 서버에서 발급
        TokenResponse resp = fetchAnonymousToken(namespace, repo);

        // 3) TTL 설정하고 저장 (expires_in 에서 60초 마진)
        long ttl = Math.max(resp.getExpiresIn() - 60, 0);
        redisTemplate.opsForValue().set(key, resp.getToken(), ttl, TimeUnit.SECONDS);
        log.debug("Cached Docker token for {}/{} (TTL={}s)", namespace, repo, ttl);

        return resp.getToken();
    }

    private TokenResponse fetchAnonymousToken(String namespace, String repo) {
        try {
            log.debug("Requesting anonymous Docker token for {}/{}", namespace, repo);

            return dockerAuthWebClient.get()
                    .uri(uri -> uri
                            .path("/token")
                            .queryParam("service", "registry.docker.io")
                            .queryParam("scope", "repository:" + namespace + "/" + repo + ":pull")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            clientRes -> clientRes.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Failed to fetch Docker token: {} for {}/{} → {}", clientRes.statusCode(), namespace, repo, body);
                                        return Mono.error(new BusinessException(ErrorCode.DOCKER_TOKEN_API_FAILED));
                                    })
                    )
                    .bodyToMono(TokenResponse.class)
                    .block();
        } catch (Exception ex) {
            log.error("Exception while fetching Docker token for {}/{}", namespace, repo, ex);
            throw new BusinessException(ErrorCode.DOCKER_TOKEN_API_FAILED);
        }
    }
}
