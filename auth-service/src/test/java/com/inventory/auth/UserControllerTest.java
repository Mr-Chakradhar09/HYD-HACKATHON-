package com.inventory.auth;

import com.inventory.auth.controller.UserController;
import com.inventory.auth.dto.*;
import com.inventory.auth.enums.Role;
import com.inventory.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUser_ShouldReturnCreated() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmployeeId("EMP001");
        req.setFirstName("John");
        req.setEmail("john@test.com");
        req.setRole(Role.WAREHOUSE_MANAGER);

        UserResponse mockRes = new UserResponse();
        when(authService.createUser(req)).thenReturn(mockRes);

        ResponseEntity<UserResponse> res = userController.createUser(req);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        when(authService.getAllUsers()).thenReturn(List.of(new UserResponse(), new UserResponse()));

        ResponseEntity<List<UserResponse>> res = userController.getAllUsers();
        assertEquals(2, res.getBody().size());
    }

    @Test
    void getUserById_ShouldReturnUser() {
        UserResponse mockRes = new UserResponse();
        when(authService.getUserById(1L)).thenReturn(mockRes);

        ResponseEntity<UserResponse> res = userController.getUserById(1L);
        assertTrue(res.getStatusCode().is2xxSuccessful());
    }

    @Test
    void getUsersByWarehouse_ShouldReturnFiltered() {
        when(authService.getUsersByWarehouse(1L)).thenReturn(List.of(new UserResponse()));

        ResponseEntity<List<UserResponse>> res = userController.getUsersByWarehouse(1L);
        assertEquals(1, res.getBody().size());
    }

    @Test
    void updateUser_ShouldReturnUpdated() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Updated");

        UserResponse mockRes = new UserResponse();
        when(authService.updateUser(1L, req)).thenReturn(mockRes);

        ResponseEntity<UserResponse> res = userController.updateUser(1L, req);
        assertTrue(res.getStatusCode().is2xxSuccessful());
    }

    @Test
    void activateUser_ShouldSucceed() {
        when(authService.activateUser(1L)).thenReturn(new UserResponse());
        assertTrue(userController.activateUser(1L).getStatusCode().is2xxSuccessful());
    }

    @Test
    void deactivateUser_ShouldSucceed() {
        when(authService.deactivateUser(1L)).thenReturn(new UserResponse());
        assertTrue(userController.deactivateUser(1L).getStatusCode().is2xxSuccessful());
    }
}
