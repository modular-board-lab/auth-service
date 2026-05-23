package com.dbwp031.authservice.auth.controller

import com.dbwp031.authservice.auth.dto.LoginRequest
import com.dbwp031.authservice.auth.dto.LogoutRequest
import com.dbwp031.authservice.auth.dto.MeResponse
import com.dbwp031.authservice.auth.dto.ReissueRequest
import com.dbwp031.authservice.auth.dto.SignupRequest
import com.dbwp031.authservice.auth.dto.SignupResponse
import com.dbwp031.authservice.auth.dto.TokenResponse
import com.dbwp031.authservice.auth.service.AuthService
import com.dbwp031.authservice.auth.support.BearerTokenResolver
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): SignupResponse = authService.signup(request)

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): TokenResponse = authService.login(request)

    @PostMapping("/reissue")
    fun reissue(
        @Valid @RequestBody request: ReissueRequest,
    ): TokenResponse = authService.reissue(request.refreshToken)

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
    ): ResponseEntity<Void> {
        authService.logout(request.refreshToken)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    fun me(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
    ): MeResponse = authService.me(BearerTokenResolver.resolve(authorization))
}
