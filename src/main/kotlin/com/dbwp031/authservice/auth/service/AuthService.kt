package com.dbwp031.authservice.auth.service

import com.dbwp031.authservice.auth.dto.LoginRequest
import com.dbwp031.authservice.auth.dto.MeResponse
import com.dbwp031.authservice.auth.dto.SignupRequest
import com.dbwp031.authservice.auth.dto.SignupResponse
import com.dbwp031.authservice.auth.dto.TokenResponse
import com.dbwp031.authservice.auth.token.JwtTokenProvider
import com.dbwp031.authservice.auth.token.RefreshTokenService
import com.dbwp031.authservice.common.exception.BusinessException
import com.dbwp031.authservice.common.exception.ErrorCode
import com.dbwp031.authservice.common.security.JwtProperties
import com.dbwp031.authservice.role.service.RoleService
import com.dbwp031.authservice.user.domain.User
import com.dbwp031.authservice.user.domain.UserStatus
import com.dbwp031.authservice.user.repository.UserRepository
import com.dbwp031.authservice.user.service.UserService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val jwtProperties: JwtProperties,
) {
    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        }

        val user = userRepository.save(
            User(
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password),
                nickname = request.nickname,
            ),
        )
        roleService.assignDefaultUserRole(user.id)

        return SignupResponse(
            userId = user.id,
            email = user.email,
            nickname = user.nickname,
        )
    }

    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email) ?: throw BusinessException(ErrorCode.INVALID_CREDENTIALS)
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw BusinessException(ErrorCode.INVALID_CREDENTIALS)
        }
        ensureActive(user)

        return issueTokens(user)
    }

    @Transactional
    fun reissue(refreshToken: String): TokenResponse {
        val savedToken = refreshTokenService.findByRawToken(refreshToken) ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        if (savedToken.revoked) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
        if (!savedToken.expiresAt.isAfter(LocalDateTime.now())) {
            savedToken.revoke()
            throw BusinessException(ErrorCode.EXPIRED_TOKEN)
        }

        val user = userService.getById(savedToken.userId)
        ensureActive(user)
        savedToken.revoke()

        return issueTokens(user)
    }

    @Transactional
    fun logout(refreshToken: String) {
        refreshTokenService.findByRawToken(refreshToken)?.revoke()
    }

    @Transactional(readOnly = true)
    fun me(accessToken: String): MeResponse {
        val claims = jwtTokenProvider.parseAccessToken(accessToken)
        val user = userService.getById(claims.userId)
        return MeResponse(
            userId = user.id,
            email = user.email,
            nickname = user.nickname,
            status = user.status,
            roles = roleService.getRoleCodes(user.id),
        )
    }

    private fun issueTokens(user: User): TokenResponse {
        val roles = roleService.getRoleCodes(user.id)
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(user, roles),
            refreshToken = refreshTokenService.issue(user.id),
            expiresIn = jwtProperties.accessTokenValiditySeconds,
        )
    }

    private fun ensureActive(user: User) {
        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(ErrorCode.INACTIVE_USER)
        }
    }
}
