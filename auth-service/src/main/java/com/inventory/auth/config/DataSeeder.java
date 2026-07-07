package com.inventory.auth.config;

import com.inventory.auth.entity.User;
import com.inventory.auth.enums.Role;
import com.inventory.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository; this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@inventory.com")) {
            User admin = new User("ADM001", "System", "Admin", "admin@inventory.com", "9999999999",
                    passwordEncoder.encode("admin123"), Role.SYSTEM_ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin user created: admin@inventory.com / admin123");
        }
        if (!userRepository.existsByEmail("manager@inventory.com")) {
            User manager = new User("MGR001", "Inventory", "Manager", "manager@inventory.com", "9999999998",
                    passwordEncoder.encode("manager123"), Role.INVENTORY_MANAGER);
            manager.setWarehouseId(1L);
            userRepository.save(manager);
        }
        if (!userRepository.existsByEmail("warehouse@inventory.com")) {
            User operator = new User("WHM001", "Warehouse", "Manager", "warehouse@inventory.com", "9999999997",
                    passwordEncoder.encode("warehouse123"), Role.WAREHOUSE_MANAGER);
            operator.setWarehouseId(1L);
            userRepository.save(operator);
        }
        if (!userRepository.existsByEmail("procurement@inventory.com")) {
            User procurement = new User("PRC001", "Procurement", "Manager", "procurement@inventory.com", "9999999996",
                    passwordEncoder.encode("procurement123"), Role.PROCUREMENT_MANAGER);
            userRepository.save(procurement);
        }
    }
}
