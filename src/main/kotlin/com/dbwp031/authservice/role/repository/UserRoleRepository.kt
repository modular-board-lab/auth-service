package com.dbwp031.authservice.role.repository

import com.dbwp031.authservice.role.domain.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRoleRepository : JpaRepository<UserRole, Long> {
    @Query(
        """
        select r.code
        from UserRole ur
        join Role r on r.id = ur.roleId
        where ur.userId = :userId
        order by r.code
        """,
    )
    fun findRoleCodesByUserId(userId: Long): List<String>
}
