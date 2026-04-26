package com.xxgwy.modules.user.service

import com.xxgwy.common.exception.BizException
import com.xxgwy.common.security.JwtConfig
import com.xxgwy.common.security.JwtUtil
import com.xxgwy.infrastructure.redis.TokenService
import com.xxgwy.modules.user.dto.*
import com.xxgwy.modules.user.entity.User
import com.xxgwy.modules.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val jwtConfig: JwtConfig,
    private val tokenService: TokenService
) {

    fun register(request: RegisterRequest): LoginResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw BizException.conflict("用户名已存在")
        }

        val user = User().apply {
            username = request.username
            password = passwordEncoder.encode(request.password)
            nickname = request.nickname ?: request.username
            role = User.Role.CUSTOMER
        }

        userRepository.save(user)
        return buildLoginResponse(user)
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { BizException.unauthorized("用户名或密码错误") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BizException.unauthorized("用户名或密码错误")
        }

        return buildLoginResponse(user)
    }

    fun getCurrentUser(userId: UUID): UserInfoResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BizException.notFound("用户不存在") }
        return UserInfoResponse.from(user)
    }

    fun refreshToken(refreshToken: String): LoginResponse {
        val userId = tokenService.validateRefreshToken(refreshToken)
            ?: throw BizException.unauthorized("refreshToken 无效或已过期")

        val user = userRepository.findById(userId)
            .orElseThrow { BizException.notFound("用户不存在") }

        tokenService.deleteRefreshToken(refreshToken)
        return buildLoginResponse(user)
    }

    private fun buildLoginResponse(user: User): LoginResponse {
        val userId = user.id ?: throw BizException.badRequest("用户ID为空")
        val accessToken = jwtUtil.createToken(userId, user.role.name)
        val refreshToken = jwtUtil.createToken(userId, user.role.name, jwtConfig.refreshExpirationHours)
        tokenService.saveRefreshToken(userId, refreshToken, jwtConfig.refreshExpirationHours)
        return LoginResponse(accessToken, refreshToken, UserInfoResponse.from(user))
    }
}
