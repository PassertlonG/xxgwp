package com.xxgwy.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String REFRESH_PREFIX = "refresh_token:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRefreshToken(UUID userId, String refreshToken, long expirationHours) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + refreshToken,
                userId.toString(),
                expirationHours,
                TimeUnit.HOURS
        );
    }

    public UUID validateRefreshToken(String refreshToken) {
        String userId = (String) redisTemplate.opsForValue().get(REFRESH_PREFIX + refreshToken);
        if (userId == null) {
            return null;
        }
        return UUID.fromString(userId);
    }

    public void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete(REFRESH_PREFIX + refreshToken);
    }
}
