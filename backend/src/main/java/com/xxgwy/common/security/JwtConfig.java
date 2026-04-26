package com.xxgwy.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    private String secret = "change-me-in-production-change-me-in-production!!";
    private long expirationHours = 24;
    private long refreshExpirationHours = 168; // 7 days
}
