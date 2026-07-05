package com.virtusa.userservice.dto.response;

import com.virtusa.userservice.enums.Location;
import com.virtusa.userservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private Location location;
    private String department;
    private String businessUnit;
    private String designation;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
