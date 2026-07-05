package com.virtusa.userservice.service;

import com.virtusa.userservice.dto.request.CreateUserRequestDto;
import com.virtusa.userservice.dto.request.UpdateUserRequestDto;
import com.virtusa.userservice.dto.response.UserResponseDto;
import com.virtusa.userservice.enums.Location;
import com.virtusa.userservice.enums.Role;

import java.util.List;

public interface UserService {

    UserResponseDto createEmployee(CreateUserRequestDto request, Role callerRole, Location callerLocation);

    UserResponseDto getEmployeeById(Long id);

    UserResponseDto getEmployeeByEmployeeId(String employeeId);

    UserResponseDto getEmployeeByEmail(String email);

    List<UserResponseDto> getEmployeesByLocation(Location location);

    List<UserResponseDto> getEmployeesByRole(Role role);

    UserResponseDto updateEmployee(Long id, UpdateUserRequestDto request, Role callerRole, Location callerLocation);

    UserResponseDto deactivateEmployee(Long id, Role callerRole, Location callerLocation);
}
