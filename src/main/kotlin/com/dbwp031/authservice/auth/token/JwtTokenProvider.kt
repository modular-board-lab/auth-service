package com.dbwp031.authservice.auth.token

import com.dbwp031.authservice.common.exception.BusinessException
import com.dbwp031.authservice.common.exception.ErrorCode
import com.dbwp031.authservice.common.security.JwtProperties
import com.dbwp031.authservice.user.domain.User
import com.dbwp031.authservice.user.domain.UserStatus
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
    private val objectMapper: ObjectMapper,
) {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun createAccessToken(
        user: User,
        roles: List<String>,
    ): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(jwtProperties.accessTokenValiditySeconds)
        val header = mapOf("alg" to "HS256", "typ" to "JWT")
        val payload = mapOf(
            "iss" to jwtProperties.issuer,
            "sub" to user.id.toString(),
            "email" to user.email,
            "roles" to roles,
            "status" to user.status.name,
            "iat" to now.epochSecond,
            "exp" to expiresAt.epochSecond,
        )

        val unsignedToken = "${encodeJson(header)}.${encodeJson(payload)}"
        return "$unsignedToken.${sign(unsignedToken)}"
    }

    fun parseAccessToken(token: String): AccessTokenClaims {
        val parts = token.split(".")
        if (parts.size != 3) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        val unsignedToken = "${parts[0]}.${parts[1]}"
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        val claims = runCatching {
            objectMapper.readValue(String(decoder.decode(parts[1]), Charsets.UTF_8), MAP_TYPE)
        }.getOrElse {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        if (claims["iss"] != jwtProperties.issuer) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        val expiresAt = numberClaim(claims, "exp")
        if (Instant.now().epochSecond >= expiresAt) {
            throw BusinessException(ErrorCode.EXPIRED_TOKEN)
        }

        return AccessTokenClaims(
            userId = (claims["sub"] as? String)?.toLongOrNull() ?: throw BusinessException(ErrorCode.INVALID_TOKEN),
            email = claims["email"] as? String ?: throw BusinessException(ErrorCode.INVALID_TOKEN),
            roles = stringListClaim(claims, "roles"),
            status = UserStatus.valueOf(claims["status"] as? String ?: throw BusinessException(ErrorCode.INVALID_TOKEN)),
        )
    }

    private fun encodeJson(value: Any): String =
        encoder.encodeToString(objectMapper.writeValueAsBytes(value))

    private fun sign(value: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(jwtProperties.secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return encoder.encodeToString(mac.doFinal(value.toByteArray(Charsets.UTF_8)))
    }

    private fun constantTimeEquals(
        expected: String,
        actual: String,
    ): Boolean {
        val expectedBytes = expected.toByteArray(Charsets.UTF_8)
        val actualBytes = actual.toByteArray(Charsets.UTF_8)
        return MessageDigestIsEqual.isEqual(expectedBytes, actualBytes)
    }

    private fun numberClaim(
        claims: Map<String, Any>,
        key: String,
    ): Long =
        when (val value = claims[key]) {
            is Number -> value.toLong()
            else -> throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

    private fun stringListClaim(
        claims: Map<String, Any>,
        key: String,
    ): List<String> {
        val values = claims[key] as? List<*> ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        return values.map { it as? String ?: throw BusinessException(ErrorCode.INVALID_TOKEN) }
    }

    private object MessageDigestIsEqual {
        fun isEqual(
            expected: ByteArray,
            actual: ByteArray,
        ): Boolean = java.security.MessageDigest.isEqual(expected, actual)
    }

    companion object {
        private val MAP_TYPE = object : TypeReference<Map<String, Any>>() {}
    }
}

data class AccessTokenClaims(
    val userId: Long,
    val email: String,
    val roles: List<String>,
    val status: UserStatus,
)
