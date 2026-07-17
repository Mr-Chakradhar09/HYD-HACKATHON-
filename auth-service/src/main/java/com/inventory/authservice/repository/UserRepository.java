package com.inventory.authservice.repository;

import com.inventory.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmployee_Email(String email);
    boolean existsByUsername(String username);
    boolean existsByEmployee_EmployeeCode(String employeeCode);
    boolean existsByEmployee_Email(String email);
    Optional<User> findByEmployee_EmployeeCode(String employeeCode);

}