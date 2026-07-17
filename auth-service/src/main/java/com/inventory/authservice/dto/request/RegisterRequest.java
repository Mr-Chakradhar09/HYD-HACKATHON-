package com.inventory.authservice.dto.request;

import com.inventory.authservice.enums.RoleType;
import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank(message = "Employee code is required")
    @Pattern(
            regexp = "^EMP[0-9]{6}$",
            message = "Employee code must be in format EMP123456"
    )
    private String employeeCode;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Full Name is required")
    @Size(min = 3, max = 100)
    private String fullName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20)
    private String password;

    public RegisterRequest(String employeeCode, String email, String fullName, String password) {
        this.employeeCode = employeeCode;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
    }

    public RegisterRequest() {
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}