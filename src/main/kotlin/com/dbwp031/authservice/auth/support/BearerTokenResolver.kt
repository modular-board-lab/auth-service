package com.dbwp031.authservice.auth.support

import com.dbwp031.authservice.common.exception.BusinessException
import com.dbwp031.authservice.common.exception.ErrorCode

object BearerTokenResolver {
    fun resolve(authorization: String?): String {
        if (authorization.isNullOrBlank()) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        val parts = authorization.trim().split(" ", limit = 2)
        if (parts.size != 2 || !parts[0].equals("Bearer", ignoreCase = true) || parts[1].isBlank()) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        return parts[1]
    }
}
