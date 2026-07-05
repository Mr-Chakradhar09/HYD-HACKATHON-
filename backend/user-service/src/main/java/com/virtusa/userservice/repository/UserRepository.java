package com.virtusa.userservice.repository;

import com.virtusa.userservice.entity.User;
import com.virtusa.userservice.enums.Location;
import com.virtusa.userservice.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmployeeId(String employeeId);

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByLocation(Location location);

    List<User> findByLocationAndRole(Location location, Role role);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
}
