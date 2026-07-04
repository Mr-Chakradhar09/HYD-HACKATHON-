package com.inventory.auth.dto;

import com.inventory.auth.enums.Role;

public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String mobile;
    private Role role;
    private Long warehouseId;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
}
