package com.xxgwy.infrastructure.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class TokenService(private val redisTemplate: RedisTemplate<String, Any>) {

    companion object {
        private const val REFRESH_PREFIX = "refresh_token:"
    }

    fun saveRefreshToken(userId: UUID, refreshToken: String, expirationHours: Long) {
        redisTemplate.opsForValue().set(
            REFRESH_PREFIX + refreshToken,
            userId.toString(),
            expirationHours,
            TimeUnit.HOURS
        )
    }

    fun validateRefreshToken(refreshToken: String): UUID? {
        val userId = redisTemplate.opsForValue().get(REFRESH_PREFIX + refreshToken) as? String ?: return null
        return UUID.fromString(userId)
    }

    fun deleteRefreshToken(refreshToken: String) {
        redisTemplate.delete(REFRESH_PREFIX + refreshToken)
    }
}
