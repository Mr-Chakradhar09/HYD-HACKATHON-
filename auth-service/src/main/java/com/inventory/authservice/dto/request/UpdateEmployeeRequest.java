package com.inventory.authservice.dto.request;

import com.inventory.authservice.enums.RoleType;
import jakarta.validation.constraints.*;

import java.util.Set;

public class    UpdateEmployeeRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotEmpty(message = "At least one role is required")
    private Set<RoleType> roles;

    public UpdateEmployeeRequest() {
    }

    public UpdateEmployeeRequest(String fullName, String email, Set<RoleType> roles) {
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
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
// Constructors
    // Getters & Setters
}