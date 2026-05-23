package com.dbwp031.authservice.role.repository

import com.dbwp031.authservice.role.domain.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> {
    fun findByCode(code: String): Role?
}
