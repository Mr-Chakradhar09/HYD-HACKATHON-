package com.inventory.auth.service;

import com.inventory.auth.dto.*;
import com.inventory.auth.entity.User;
import com.inventory.auth.enums.Role;
import com.inventory.auth.enums.UserStatus;
import com.inventory.auth.repository.UserRepository;
import com.inventory.auth.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("User is not active");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user);
        return new LoginResponse(token, user.getEmail(), user.getRole().name(), user.getFirstName(), user.getLastName(), user.getWarehouseId());
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists");
        }

        User user = new User(
                request.getEmployeeId(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getMobile(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        user.setWarehouseId(request.getWarehouseId());
        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getMobile() != null) user.setMobile(request.getMobile());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getWarehouseId() != null) user.setWarehouseId(request.getWarehouseId());

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.INACTIVE);
        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public List<UserResponse> getUsersByWarehouse(Long warehouseId) {
        return userRepository.findByWarehouseId(warehouseId).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }
}
