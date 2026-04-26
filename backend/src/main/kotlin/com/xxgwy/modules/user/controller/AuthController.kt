package com.xxgwy.modules.user.controller

import com.xxgwy.common.response.R
import com.xxgwy.modules.user.dto.*
import com.xxgwy.modules.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
class AuthController(private val userService: UserService) {

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): R<LoginResponse> =
        R.ok(userService.register(request))

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): R<LoginResponse> =
        R.ok(userService.login(request))

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal userId: UUID): R<UserInfoResponse> =
        R.ok(userService.getCurrentUser(userId))

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): R<LoginResponse> =
        R.ok(userService.refreshToken(request.refreshToken))
}
