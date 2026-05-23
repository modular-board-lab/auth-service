package com.dbwp031.authservice.role.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "roles",
    uniqueConstraints = [UniqueConstraint(name = "uk_roles_code", columnNames = ["code"])],
)
class Role(
    @Column(nullable = false, length = 50)
    val code: String,

    @Column(nullable = false, length = 100)
    val name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
        protected set
}
