package com.dbwp031.authservice.auth.dto

import com.dbwp031.authservice.user.domain.UserStatus

data class SignupResponse(
    val userId: Long,
    val email: String,
    val nickname: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
)

data class MeResponse(
    val userId: Long,
    val email: String,
    val nickname: String,
    val status: UserStatus,
    val roles: List<String>,
)
