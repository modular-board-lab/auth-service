package com.dbwp031.authservice.common.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val issuer: String,
    val secret: String,
    val accessTokenValiditySeconds: Long,
    val refreshTokenValiditySeconds: Long,
)
