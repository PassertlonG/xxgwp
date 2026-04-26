package com.xxgwy.modules.user.controller;

import com.xxgwy.common.response.R;
import com.xxgwy.modules.user.dto.*;
import com.xxgwy.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return R.ok(userService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(userService.login(request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public R<UserInfoResponse> me(@AuthenticationPrincipal UUID userId) {
        return R.ok(userService.getCurrentUser(userId));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public R<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return R.ok(userService.refreshToken(request.getRefreshToken()));
    }
}
