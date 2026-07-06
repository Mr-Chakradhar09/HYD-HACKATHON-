package com.inventory.auth;

import com.inventory.auth.controller.AuthController;
import com.inventory.auth.dto.LoginRequest;
import com.inventory.auth.dto.LoginResponse;
import com.inventory.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ShouldReturnOk() {
        LoginRequest req = new LoginRequest("admin@test.com", "password");
        LoginResponse mockRes = new LoginResponse("token", "admin@test.com", "SYSTEM_ADMIN", "Admin", "User", null);

        when(authService.login(req)).thenReturn(mockRes);

        ResponseEntity<LoginResponse> res = authController.login(req);
        assertTrue(res.getStatusCode().is2xxSuccessful());
        assertEquals("token", res.getBody().getToken());
    }
}
