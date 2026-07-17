package com.inventory.authservice.initializer;

import com.inventory.authservice.entity.Employee;
import com.inventory.authservice.entity.Role;
import com.inventory.authservice.entity.User;
import com.inventory.authservice.enums.EmployeeStatus;
import com.inventory.authservice.enums.RoleType;
import com.inventory.authservice.repository.EmployeeRepository;
import com.inventory.authservice.repository.RoleRepository;
import com.inventory.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // Seed Roles
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByRoleName(roleType)) {
                Role role = new Role();
                role.setRoleName(roleType);
                roleRepository.save(role);
            }
        }

        // Seed Default Admin
        if (!userRepository.existsByUsername("admin")) {
            Employee employee = new Employee();
            employee.setEmployeeCode("EMP000001");
            employee.setFullName("System Administrator");
            employee.setEmail("admin@inventory.com");
            employee.setStatus(EmployeeStatus.ACTIVE);
            employee.setCreatedBy("SYSTEM");
            Role adminRole = roleRepository.findByRoleName(RoleType.SYSTEM_ADMIN).orElseThrow();
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            employee.setRoles(roles);
            employeeRepository.save(employee);
            
            User user = new User();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("Admin@123"));
            user.setEnabled(true);
            user.setEmployee(employee);
            userRepository.save(user);
            System.out.println("Default Admin User Created");
        }

        // Seed Default Manager
        if (!userRepository.existsByUsername("manager")) {
            Employee managerEmp = new Employee();
            managerEmp.setEmployeeCode("EMP000002");
            managerEmp.setFullName("Inventory Manager");
            managerEmp.setEmail("manager@inventory.com");
            managerEmp.setStatus(EmployeeStatus.ACTIVE);
            managerEmp.setCreatedBy("SYSTEM");
            Role managerRole = roleRepository.findByRoleName(RoleType.INVENTORY_MANAGER).orElseThrow();
            Set<Role> managerRoles = new HashSet<>();
            managerRoles.add(managerRole);
            managerEmp.setRoles(managerRoles);
            employeeRepository.save(managerEmp);
            
            User managerUser = new User();
            managerUser.setUsername("manager");
            managerUser.setPassword(passwordEncoder.encode("Password@123"));
            managerUser.setEnabled(true);
            managerUser.setEmployee(managerEmp);
            userRepository.save(managerUser);
            System.out.println("Default Manager User Created");
        }
    }
}