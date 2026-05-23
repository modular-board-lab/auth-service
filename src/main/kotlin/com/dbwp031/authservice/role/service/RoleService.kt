package com.dbwp031.authservice.role.service

import com.dbwp031.authservice.common.exception.BusinessException
import com.dbwp031.authservice.common.exception.ErrorCode
import com.dbwp031.authservice.role.domain.UserRole
import com.dbwp031.authservice.role.repository.RoleRepository
import com.dbwp031.authservice.role.repository.UserRoleRepository
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val userRoleRepository: UserRoleRepository,
) {
    fun assignDefaultUserRole(userId: Long) {
        val role = roleRepository.findByCode("USER") ?: throw BusinessException(ErrorCode.ROLE_NOT_FOUND)
        userRoleRepository.save(UserRole(userId = userId, roleId = role.id))
    }

    fun getRoleCodes(userId: Long): List<String> = userRoleRepository.findRoleCodesByUserId(userId)
}
