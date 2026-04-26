package com.xxgwy.common.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
class JwtConfig {
    var secret: String = "change-me-in-production-change-me-in-production!!"
    var expirationHours: Long = 24
    var refreshExpirationHours: Long = 168
}
