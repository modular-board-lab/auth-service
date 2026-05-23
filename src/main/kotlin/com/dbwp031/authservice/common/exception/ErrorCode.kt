package com.dbwp031.authservice.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "Email already exists."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Expired token."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "User account is not active."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found."),
    ROLE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "Default role is not initialized."),
}
