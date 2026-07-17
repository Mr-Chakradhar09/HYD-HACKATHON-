package com.inventory.authservice.dto.response;

import java.util.List;

public class UserResponse {


    private Long id;

    private String employeeCode;

    private String username;

    private String email;

    private boolean enabled;

    private List<String> roles;



    public UserResponse() {
    }



    public UserResponse(
            Long id,
            String employeeCode,
            String username,
            String email,
            boolean enabled,
            List<String> roles) {

        this.id = id;
        this.employeeCode = employeeCode;
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
    }



    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }



    public String getEmployeeCode() {
        return employeeCode;
    }


    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }



    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }



    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }



    public boolean isEnabled() {
        return enabled;
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }



    public List<String> getRoles() {
        return roles;
    }


    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}