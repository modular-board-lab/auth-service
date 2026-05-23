package com.dbwp031.authservice.role.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "user_roles",
    uniqueConstraints = [UniqueConstraint(name = "uk_user_roles_user_role", columnNames = ["user_id", "role_id"])],
    indexes = [
        Index(name = "idx_user_roles_user_id", columnList = "user_id"),
        Index(name = "idx_user_roles_role_id", columnList = "role_id"),
    ],
)
class UserRole(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "role_id", nullable = false)
    val roleId: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
        protected set
}
