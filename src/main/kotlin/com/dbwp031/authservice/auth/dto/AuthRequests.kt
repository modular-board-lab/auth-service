package com.dbwp031.authservice.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:Email(message = "Email format is invalid.")
    @field:NotBlank(message = "Email is required.")
    val email: String,

    @field:NotBlank(message = "Password is required.")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    val password: String,

    @field:NotBlank(message = "Nickname is required.")
    @field:Size(max = 100, message = "Nickname must be 100 characters or less.")
    val nickname: String,
)

data class LoginRequest(
    @field:Email(message = "Email format is invalid.")
    @field:NotBlank(message = "Email is required.")
    val email: String,

    @field:NotBlank(message = "Password is required.")
    val password: String,
)

data class ReissueRequest(
    @field:NotBlank(message = "Refresh token is required.")
    val refreshToken: String,
)

data class LogoutRequest(
    @field:NotBlank(message = "Refresh token is required.")
    val refreshToken: String,
)
