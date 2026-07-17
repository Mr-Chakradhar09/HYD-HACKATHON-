package com.inventory.authservice.initializer;

import com.inventory.authservice.entity.Role;
import com.inventory.authservice.enums.RoleType;
import com.inventory.authservice.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {

        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByRoleName(roleType)) {
                Role role = new Role();
                role.setRoleName(roleType);
                roleRepository.save(role);
            }
        }
    }
}
