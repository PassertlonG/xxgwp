package com.xxgwy.modules.user.controller

import com.xxgwy.common.exception.BizException
import com.xxgwy.common.response.R
import com.xxgwy.modules.user.dto.*
import com.xxgwy.modules.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
class AuthController(private val userService: UserService) {

    @Operation(summary = "用户注册", responses = [
        ApiResponse(responseCode = "200", description = "注册成功"),
        ApiResponse(responseCode = "409", description = "用户名已存在", content = [Content(schema = Schema(implementation = R::class))]),
        ApiResponse(responseCode = "400", description = "参数校验失败", content = [Content(schema = Schema(implementation = R::class))])
    ])
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): R<LoginResponse> =
        R.ok(userService.register(request))

    @Operation(summary = "用户登录", responses = [
        ApiResponse(responseCode = "200", description = "登录成功"),
        ApiResponse(responseCode = "401", description = "用户名或密码错误", content = [Content(schema = Schema(implementation = R::class))])
    ])
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): R<LoginResponse> =
        R.ok(userService.login(request))

    @Operation(summary = "获取当前用户信息", responses = [
        ApiResponse(responseCode = "200", description = "成功"),
        ApiResponse(responseCode = "401", description = "未登录或Token无效", content = [Content(schema = Schema(implementation = R::class))]),
        ApiResponse(responseCode = "404", description = "用户不存在", content = [Content(schema = Schema(implementation = R::class))])
    ])
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal userId: UUID?): R<UserInfoResponse> {
        return R.ok(userService.getCurrentUser(userId ?: throw BizException.unauthorized("未登录")))
    }

    @Operation(summary = "刷新 Token", responses = [
        ApiResponse(responseCode = "200", description = "刷新成功"),
        ApiResponse(responseCode = "401", description = "refreshToken 无效或已过期", content = [Content(schema = Schema(implementation = R::class))])
    ])
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): R<LoginResponse> =
        R.ok(userService.refreshToken(request.refreshToken))
}
