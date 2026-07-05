package com.virtusa.userservice.service.impl;

import com.virtusa.userservice.dto.request.CreateUserRequestDto;
import com.virtusa.userservice.dto.request.UpdateUserRequestDto;
import com.virtusa.userservice.dto.response.UserResponseDto;
import com.virtusa.userservice.entity.User;
import com.virtusa.userservice.enums.Location;
import com.virtusa.userservice.enums.Role;
import com.virtusa.userservice.exception.DuplicateResourceException;
import com.virtusa.userservice.exception.ResourceNotFoundException;
import com.virtusa.userservice.exception.UnauthorizedException;
import com.virtusa.userservice.mapper.UserMapper;
import com.virtusa.userservice.repository.UserRepository;
import com.virtusa.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto createEmployee(CreateUserRequestDto request, Role callerRole, Location callerLocation) {
        log.info("Processing onboarding request for employeeId: {}, requested by role: {}, location: {}", 
                request.getEmployeeId(), callerRole, callerLocation);

        // 1. Enforce Authorization Logic
        validateOnboardingAuthority(request, callerRole, callerLocation);

        // 2. Enforce Uniqueness constraints
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            log.warn("Onboarding failed: Employee ID {} already exists", request.getEmployeeId());
            throw new DuplicateResourceException("Employee ID '" + request.getEmployeeId() + "' already exists.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Onboarding failed: Email {} already exists", request.getEmail());
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' already exists.");
        }

        // 3. Save User
        User user = userMapper.toEntity(request);
        user.setActive(true); // Always active on onboarding
        
        User savedUser = userRepository.save(user);
        log.info("Successfully onboarded user. Database ID: {}, Employee ID: {}", savedUser.getId(), savedUser.getEmployeeId());
        
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getEmployeeById(Long id) {
        log.debug("Fetching user profile by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getEmployeeByEmployeeId(String employeeId) {
        log.debug("Fetching user profile by Employee ID: {}", employeeId);
        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Employee ID: " + employeeId));
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getEmployeeByEmail(String email) {
        log.debug("Fetching user profile by Email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Email: " + email));
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getEmployeesByLocation(Location location) {
        log.debug("Fetching user profiles by Location: {}", location);
        return userRepository.findByLocation(location).stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getEmployeesByRole(Role role) {
        log.debug("Fetching user profiles by Role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDto updateEmployee(Long id, UpdateUserRequestDto request, Role callerRole, Location callerLocation) {
        log.info("Processing profile update request for database ID: {}, requested by role: {}, location: {}", 
                id, callerRole, callerLocation);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Enforce Authorization Logic for Updates
        validateUpdateAuthority(existingUser, request, callerRole, callerLocation);

        // Update fields using ModelMapper
        userMapper.updateEntity(request, existingUser);

        User updatedUser = userRepository.save(existingUser);
        log.info("Successfully updated profile for user ID: {}", updatedUser.getId());
        
        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    @Transactional
    public UserResponseDto deactivateEmployee(Long id, Role callerRole, Location callerLocation) {
        log.info("Processing deactivation request for database ID: {}, requested by role: {}, location: {}", 
                id, callerRole, callerLocation);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Enforce Authorization Logic for Deactivations
        validateDeactivateAuthority(existingUser, callerRole, callerLocation);

        existingUser.setActive(false);
        User savedUser = userRepository.save(existingUser);
        log.info("Successfully deactivated user profile ID: {}", savedUser.getId());
        
        return userMapper.toResponseDto(savedUser);
    }

    // Helper validation for onboarding users
    private void validateOnboardingAuthority(CreateUserRequestDto request, Role callerRole, Location callerLocation) {
        if (callerRole == null) {
            throw new UnauthorizedException("Caller identity role is required.");
        }

        if (callerRole == Role.EMPLOYEE) {
            throw new UnauthorizedException("EMPLOYEE users are not allowed to create or onboard profiles.");
        }

        if (callerRole == Role.HR) {
            if (request.getRole() != Role.EMPLOYEE) {
                throw new UnauthorizedException("HR users can only onboard users with role EMPLOYEE.");
            }
            if (callerLocation == null) {
                throw new UnauthorizedException("HR location configuration is missing.");
            }
            if (request.getLocation() != callerLocation) {
                throw new UnauthorizedException(String.format(
                        "HR users can only onboard employees belonging to their location (%s). Requested location: %s", 
                        callerLocation, request.getLocation()));
            }
        }
        // GLOBAL_HR can create any user role at any location.
    }

    // Helper validation for profile updates
    private void validateUpdateAuthority(User existingUser, UpdateUserRequestDto request, Role callerRole, Location callerLocation) {
        if (callerRole == null) {
            throw new UnauthorizedException("Caller identity role is required.");
        }

        if (callerRole == Role.EMPLOYEE) {
            throw new UnauthorizedException("EMPLOYEE users are not authorized to update user profiles.");
        }

        if (callerRole == Role.HR) {
            // HR can only update standard EMPLOYEE profiles
            if (existingUser.getRole() != Role.EMPLOYEE) {
                throw new UnauthorizedException("HR can only update profiles of users with role EMPLOYEE.");
            }
            // HR can only update employees from their location
            if (callerLocation == null || existingUser.getLocation() != callerLocation) {
                throw new UnauthorizedException(String.format(
                        "HR can only update employees in their own location (%s). Target employee location: %s", 
                        callerLocation, existingUser.getLocation()));
            }
            // HR cannot elevate the user role or relocate them outside HR's location
            if (request.getRole() != Role.EMPLOYEE) {
                throw new UnauthorizedException("HR cannot change the user role to values other than EMPLOYEE.");
            }
            if (request.getLocation() != callerLocation) {
                throw new UnauthorizedException(String.format(
                        "HR can only assign employees to their own location (%s). Requested location: %s", 
                        callerLocation, request.getLocation()));
            }
        }
        // GLOBAL_HR is fully authorized
    }

    // Helper validation for deactivating users
    private void validateDeactivateAuthority(User existingUser, Role callerRole, Location callerLocation) {
        if (callerRole == null) {
            throw new UnauthorizedException("Caller identity role is required.");
        }

        if (callerRole == Role.EMPLOYEE) {
            throw new UnauthorizedException("EMPLOYEE users are not authorized to deactivate users.");
        }

        if (callerRole == Role.HR) {
            // HR can only deactivate standard EMPLOYEE profiles
            if (existingUser.getRole() != Role.EMPLOYEE) {
                throw new UnauthorizedException("HR can only deactivate users with role EMPLOYEE.");
            }
            // HR can only deactivate employees from their location
            if (callerLocation == null || existingUser.getLocation() != callerLocation) {
                throw new UnauthorizedException(String.format(
                        "HR can only deactivate employees in their own location (%s). Target employee location: %s", 
                        callerLocation, existingUser.getLocation()));
            }
        }
        // GLOBAL_HR is fully authorized
    }
}
