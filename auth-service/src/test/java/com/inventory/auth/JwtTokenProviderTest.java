package com.inventory.auth;

import com.inventory.auth.entity.User;
import com.inventory.auth.enums.Role;
import com.inventory.auth.enums.UserStatus;
import com.inventory.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", "TestSecretKeyForInventoryManagementSystem2024!VeryLong");
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", 86400000L);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        User user = new User("ADM001", "Test", "User", "test@test.com", "9999999999", "password", Role.SYSTEM_ADMIN);
        user.setId(1L);

        String token = jwtTokenProvider.generateToken(user);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void generateToken_WithWarehouseId_ShouldIncludeInToken() {
        User user = new User("MGR001", "Test", "Manager", "manager@test.com", "9999999998", "password", Role.WAREHOUSE_MANAGER);
        user.setId(2L);
        user.setWarehouseId(1L);

        String token = jwtTokenProvider.generateToken(user);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", -1000L);
        User user = new User("ADM001", "Test", "User", "test@test.com", "9999999999", "password", Role.SYSTEM_ADMIN);
        user.setId(1L);

        String token = jwtTokenProvider.generateToken(user);
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void getUserIdFromToken_ShouldReturnCorrectId() {
        User user = new User("ADM001", "Test", "User", "test@test.com", "9999999999", "password", Role.SYSTEM_ADMIN);
        user.setId(42L);

        String token = jwtTokenProvider.generateToken(user);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(42L, userId);
    }
}
