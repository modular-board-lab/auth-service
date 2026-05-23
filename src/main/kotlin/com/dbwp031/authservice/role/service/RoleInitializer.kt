package com.dbwp031.authservice.role.service

import com.dbwp031.authservice.role.domain.Role
import com.dbwp031.authservice.role.repository.RoleRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RoleInitializer(
    private val roleRepository: RoleRepository,
) : ApplicationRunner {
    @Transactional
    override fun run(args: ApplicationArguments) {
        saveIfAbsent("USER", "User")
        saveIfAbsent("ADMIN", "Admin")
    }

    private fun saveIfAbsent(
        code: String,
        name: String,
    ) {
        if (roleRepository.findByCode(code) == null) {
            roleRepository.save(Role(code = code, name = name))
        }
    }
}
