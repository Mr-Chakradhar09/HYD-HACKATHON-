package com.inventory.auth.dto;

import com.inventory.auth.entity.User;
import com.inventory.auth.enums.Role;
import com.inventory.auth.enums.UserStatus;
import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private Role role;
    private UserStatus status;
    private Long warehouseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.employeeId = user.getEmployeeId();
        r.firstName = user.getFirstName();
        r.lastName = user.getLastName();
        r.email = user.getEmail();
        r.mobile = user.getMobile();
        r.role = user.getRole();
        r.status = user.getStatus();
        r.warehouseId = user.getWarehouseId();
        r.createdAt = user.getCreatedAt();
        r.updatedAt = user.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public Role getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public Long getWarehouseId() { return warehouseId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
