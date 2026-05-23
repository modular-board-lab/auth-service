package com.dbwp031.authservice.auth.token

import com.dbwp031.authservice.auth.support.TokenHashing
import com.dbwp031.authservice.common.security.JwtProperties
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties,
) {
    private val secureRandom = SecureRandom()
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    fun issue(userId: Long): String {
        val token = newOpaqueToken()
        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                tokenHash = TokenHashing.sha256(token),
                expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenValiditySeconds),
            ),
        )
        return token
    }

    fun findByRawToken(rawToken: String): RefreshToken? =
        refreshTokenRepository.findByTokenHash(TokenHashing.sha256(rawToken))

    private fun newOpaqueToken(): String {
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)
        return encoder.encodeToString(bytes)
    }
}
