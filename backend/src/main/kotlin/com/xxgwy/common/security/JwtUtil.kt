package com.xxgwy.common.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil(private val jwtConfig: JwtConfig) {

    private val key by lazy {
        Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray())
    }

    fun createToken(userId: UUID, role: String): String = createToken(userId, role, jwtConfig.expirationHours)

    fun createToken(userId: UUID, role: String, expirationHours: Long): String {
        val now = Date()
        return Jwts.builder()
            .claim("sub", userId.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(Date(now.time + expirationHours * 3600 * 1000))
            .signWith(key)
            .compact()
    }

    fun parseToken(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    fun getUserId(token: String): UUID = UUID.fromString(parseToken(token)["sub", String::class.java])
    fun getRole(token: String): String = parseToken(token)["role", String::class.java]
}
