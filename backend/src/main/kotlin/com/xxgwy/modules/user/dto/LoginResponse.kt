package com.xxgwy.modules.user.dto

import io.swagger.v3.oas.annotations.media.Schema

data class LoginResponse(
    var accessToken: String,
    @Schema(nullable = true)
    var refreshToken: String?,
    val user: UserInfoResponse
)
