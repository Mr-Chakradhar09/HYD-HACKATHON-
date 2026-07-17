package com.inventory.authservice.service;

import com.inventory.authservice.dto.request.ChangePasswordRequest;
import com.inventory.authservice.dto.request.CreateEmployeeRequest;
import com.inventory.authservice.dto.request.RegisterRequest;
import com.inventory.authservice.dto.response.CreateEmployeeResponse;
import com.inventory.authservice.dto.response.InternalEmployeeResponse;
import com.inventory.authservice.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse findByEmployeeCode(String employeeCode);

    UserResponse getCurrentUser();

    InternalEmployeeResponse getInternalEmployeeById(Long employeeId);
    //CreateEmployeeResponse createEmployee(CreateEmployeeRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    void activateUser(Long id);

    void deactivateUser(Long id);

    void changePassword(ChangePasswordRequest request);

}