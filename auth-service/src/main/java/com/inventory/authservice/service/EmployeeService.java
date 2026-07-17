package com.inventory.authservice.service;

import com.inventory.authservice.dto.request.CreateEmployeeRequest;
import com.inventory.authservice.dto.request.UpdateEmployeeRequest;
import com.inventory.authservice.dto.response.CreateEmployeeResponse;
import com.inventory.authservice.dto.response.EmployeeResponse;
import com.inventory.authservice.dto.response.InternalEmployeeResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface EmployeeService {
    CreateEmployeeResponse createEmployee(CreateEmployeeRequest request);
    EmployeeResponse getEmployeeByCode(String employeeCode);

    InternalEmployeeResponse getInternalEmployeeById(Long employeeId);
    InternalEmployeeResponse getInternalEmployeeByCode(String employeeCode);

    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse updateEmployee(String employeeCode, UpdateEmployeeRequest request);
    void activateEmployee(String employeeCode);
    void deactivateEmployee(String employeeCode);
    void deleteEmployee(String employeeCode);
}