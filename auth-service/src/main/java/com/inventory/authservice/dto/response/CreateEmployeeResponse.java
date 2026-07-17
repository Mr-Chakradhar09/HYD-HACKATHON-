package com.inventory.authservice.dto.response;

import com.inventory.authservice.enums.EmployeeStatus;
import com.inventory.authservice.enums.RoleType;

import java.util.List;

public class CreateEmployeeResponse {

    private String employeeCode;

    private String fullName;

    private String email;

    private List<String> roles;

    private EmployeeStatus status;

    private String message;

    public CreateEmployeeResponse() {
    }

    public CreateEmployeeResponse(
            String employeeCode,
            String fullName,
            String email,
            List<String> roles,
            EmployeeStatus status,
            String message) {

        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
        this.status = status;
        this.message = message;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}