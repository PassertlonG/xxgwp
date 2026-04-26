package com.xxgwy.modules.user.dto

data class LoginResponse(
    var accessToken: String,
    var refreshToken: String?,
    val user: UserInfoResponse
)
