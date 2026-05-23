package com.dbwp031.authservice.user.service

import com.dbwp031.authservice.common.exception.BusinessException
import com.dbwp031.authservice.common.exception.ErrorCode
import com.dbwp031.authservice.user.domain.User
import com.dbwp031.authservice.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getById(userId: Long): User =
        userRepository.findById(userId).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
}
