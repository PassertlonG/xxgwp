package com.xxgwy.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JwtAuthFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = header.removePrefix("Bearer ")
            val claims = jwtUtil.parseToken(token)
            val userId = UUID.fromString(claims["sub", String::class.java])
            val role = claims["role", String::class.java]

            val authentication = UsernamePasswordAuthenticationToken(userId, null, listOf())
            SecurityContextHolder.getContext().authentication = authentication

            log.debug("Authenticated user: {} role: {}", userId, role)
        } catch (e: Exception) {
            log.warn("JWT authentication failed: {}", e.message)
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}
