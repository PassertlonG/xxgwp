package com.xxgwy.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(UUID userId, String role) {
        return createToken(userId, role, jwtConfig.getExpirationHours());
    }

    public String createToken(UUID userId, String role, long expirationHours) {
        long expirationMs = expirationHours * 3600 * 1000;
        Date now = new Date();

        return Jwts.builder()
                .claim("sub", userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseToken(token).get("sub", String.class));
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }
}
