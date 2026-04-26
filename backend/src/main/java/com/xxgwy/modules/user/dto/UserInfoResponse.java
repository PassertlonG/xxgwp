package com.xxgwy.modules.user.dto;

import com.xxgwy.modules.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserInfoResponse {
    private UUID id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getRole() != null ? user.getRole().name() : null
        );
    }
}
