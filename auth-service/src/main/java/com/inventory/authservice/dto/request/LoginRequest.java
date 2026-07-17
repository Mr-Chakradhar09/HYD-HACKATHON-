package com.inventory.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginRequest {

    @NotBlank(message = "Employee code is required")
    @Pattern(
            regexp = "^EMP\\d{6}$",
            message = "Employee ID must be in format EMP123456"
    )
    private String employeeCode;


    @NotBlank(message = "Password is required")
    private String password;


    public LoginRequest() {
    }


    public LoginRequest(String employeeCode, String password) {
        this.employeeCode = employeeCode;
        this.password = password;
    }


    public String getEmployeeCode() {
        return employeeCode;
    }


    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }
}