package com.xxgwy.modules.user.dto

import com.xxgwy.modules.user.entity.User
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UserInfoResponse(
    @Schema(nullable = true)
    val id: UUID?,
    val username: String,
    val nickname: String,
    @Schema(nullable = true)
    val avatar: String?,
    val role: String
) {
    companion object {
        fun from(user: User): UserInfoResponse = UserInfoResponse(
            id = user.id,
            username = user.username,
            nickname = user.nickname,
            avatar = user.avatar,
            role = user.role.name
        )
    }
}
