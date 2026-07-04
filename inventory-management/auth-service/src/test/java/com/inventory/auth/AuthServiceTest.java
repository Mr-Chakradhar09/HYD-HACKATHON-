package com.inventory.auth;

import com.inventory.auth.dto.*;
import com.inventory.auth.entity.User;
import com.inventory.auth.enums.Role;
import com.inventory.auth.enums.UserStatus;
import com.inventory.auth.repository.UserRepository;
import com.inventory.auth.security.JwtTokenProvider;
import com.inventory.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void createUser_ShouldCreateSuccessfully() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmployeeId("EMP001");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@test.com");
        req.setMobile("1234567890");
        req.setPassword("password123");
        req.setRole(Role.INVENTORY_MANAGER);
        req.setWarehouseId(1L);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByEmployeeId(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = authService.createUser(req);

        assertNotNull(response);
        assertEquals("EMP001", response.getEmployeeId());
        assertEquals("John", response.getFirstName());
        assertEquals(Role.INVENTORY_MANAGER, response.getRole());
        assertEquals(1L, response.getWarehouseId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrow() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("dup@test.com");
        req.setEmployeeId("EMP002");

        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.createUser(req));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        String encodedPwd = passwordEncoder.encode("password123");
        User user = new User("EMP001", "John", "Doe", "john@test.com", "1234567890", encodedPwd, Role.WAREHOUSE_MANAGER);
        user.setId(1L);
        user.setWarehouseId(1L);

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user)).thenReturn("test-jwt-token");

        LoginRequest loginReq = new LoginRequest("john@test.com", "password123");
        LoginResponse response = authService.login(loginReq);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("john@test.com", response.getEmail());
        assertEquals("WAREHOUSE_MANAGER", response.getRole());
        assertEquals(1L, response.getWarehouseId());
    }

    @Test
    void login_WithInvalidEmail_ShouldThrow() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest("unknown@test.com", "password");
        assertThrows(RuntimeException.class, () -> authService.login(req));
    }

    @Test
    void login_WithWrongPassword_ShouldThrow() {
        String encodedPwd = passwordEncoder.encode("correctPwd");
        User user = new User("EMP001", "John", "Doe", "john@test.com", "1234567890", encodedPwd, Role.WAREHOUSE_MANAGER);

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest("john@test.com", "wrongPwd");
        assertThrows(RuntimeException.class, () -> authService.login(req));
    }

    @Test
    void login_WithInactiveUser_ShouldThrow() {
        String encodedPwd = passwordEncoder.encode("password123");
        User user = new User("EMP001", "John", "Doe", "john@test.com", "1234567890", encodedPwd, Role.WAREHOUSE_MANAGER);
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest("john@test.com", "password123");
        assertThrows(RuntimeException.class, () -> authService.login(req));
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(
                new User("EMP001", "John", "Doe", "john@test.com", "123", "pwd", Role.SYSTEM_ADMIN),
                new User("EMP002", "Jane", "Doe", "jane@test.com", "456", "pwd", Role.WAREHOUSE_MANAGER)
        ));

        List<UserResponse> users = authService.getAllUsers();
        assertEquals(2, users.size());
    }

    @Test
    void getUserById_ShouldReturnUser() {
        User user = new User("EMP001", "John", "Doe", "john@test.com", "123", "pwd", Role.SYSTEM_ADMIN);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = authService.getUserById(1L);
        assertEquals("John", response.getFirstName());
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        User user = new User("EMP001", "John", "Doe", "john@test.com", "123", "pwd", Role.SYSTEM_ADMIN);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Johnny");
        req.setRole(Role.WAREHOUSE_MANAGER);
        req.setWarehouseId(2L);

        UserResponse response = authService.updateUser(1L, req);
        assertEquals("Johnny", response.getFirstName());
        assertEquals(Role.WAREHOUSE_MANAGER, response.getRole());
        assertEquals(2L, response.getWarehouseId());
    }

    @Test
    void activateUser_ShouldSetActive() {
        User user = new User("EMP001", "John", "Doe", "john@test.com", "123", "pwd", Role.SYSTEM_ADMIN);
        user.setId(1L);
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = authService.activateUser(1L);
        assertEquals(UserStatus.ACTIVE, response.getStatus());
    }

    @Test
    void deactivateUser_ShouldSetInactive() {
        User user = new User("EMP001", "John", "Doe", "john@test.com", "123", "pwd", Role.SYSTEM_ADMIN);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = authService.deactivateUser(1L);
        assertEquals(UserStatus.INACTIVE, response.getStatus());
    }

    @Test
    void getUsersByWarehouse_ShouldReturnFilteredList() {
        User whMgr = new User("EMP001", "John", "Doe", "john@test.com", "123", "pwd", Role.WAREHOUSE_MANAGER);
        whMgr.setWarehouseId(1L);
        User invMgr = new User("EMP002", "Jane", "Doe", "jane@test.com", "456", "pwd", Role.INVENTORY_MANAGER);
        invMgr.setWarehouseId(1L);

        when(userRepository.findByWarehouseId(1L)).thenReturn(List.of(whMgr, invMgr));

        List<UserResponse> users = authService.getUsersByWarehouse(1L);
        assertEquals(2, users.size());
    }
}
