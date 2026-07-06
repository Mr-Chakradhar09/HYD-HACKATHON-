package com.inventory.auth.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private Long warehouseId;

    public LoginResponse() {}
    public LoginResponse(String token, String email, String role, String firstName, String lastName, Long warehouseId) {
        this.token = token; this.email = email; this.role = role; this.firstName = firstName; this.lastName = lastName; this.warehouseId = warehouseId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
}
