package com.virtusa.userservice.dto.request;

import com.virtusa.userservice.enums.Location;
import com.virtusa.userservice.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequestDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "Location is required")
    private Location location;

    private String department;

    private String businessUnit;

    private String designation;
}
