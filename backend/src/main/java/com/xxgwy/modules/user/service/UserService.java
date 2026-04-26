package com.xxgwy.modules.user.service;

import com.xxgwy.common.exception.BizException;
import com.xxgwy.common.security.JwtConfig;
import com.xxgwy.common.security.JwtUtil;
import com.xxgwy.infrastructure.redis.TokenService;
import com.xxgwy.modules.user.dto.*;
import com.xxgwy.modules.user.entity.User;
import com.xxgwy.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final TokenService tokenService;

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw BizException.conflict("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(User.Role.CUSTOMER);

        userRepository.save(user);
        return buildLoginResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> BizException.unauthorized("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BizException.unauthorized("用户名或密码错误");
        }

        return buildLoginResponse(user);
    }

    public UserInfoResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));
        return UserInfoResponse.from(user);
    }

    public LoginResponse refreshToken(String refreshToken) {
        UUID userId = tokenService.validateRefreshToken(refreshToken);
        if (userId == null) {
            throw BizException.unauthorized("refreshToken 无效或已过期");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));

        tokenService.deleteRefreshToken(refreshToken);
        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtUtil.createToken(user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.createToken(user.getId(), user.getRole().name(), jwtConfig.getRefreshExpirationHours());
        tokenService.saveRefreshToken(user.getId(), refreshToken, jwtConfig.getRefreshExpirationHours());
        return new LoginResponse(accessToken, refreshToken, UserInfoResponse.from(user));
    }
}

