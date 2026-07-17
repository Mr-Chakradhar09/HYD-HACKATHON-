package com.inventory.authservice.dto.request;

import com.inventory.authservice.enums.RoleType;
import jakarta.validation.constraints.*;

import java.util.Set;

public class CreateEmployeeRequest {

    @NotBlank(message = "Employee code is required")
    @Pattern(
            regexp = "^EMP[0-9]{6}$",
            message = "Employee code must be in format EMP123456"
    )
    private String employeeCode;

    private String fullName;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    @NotEmpty(message = "At least one role is required")
    private Set<RoleType> roles;
    public CreateEmployeeRequest() {
    }
    public CreateEmployeeRequest(
            String employeeCode,
            String fullName,
            String email,
            Set<RoleType> roles) {

        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
    }
    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
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

    public Set<RoleType> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleType> roles) {
        this.roles = roles;
    }
}

