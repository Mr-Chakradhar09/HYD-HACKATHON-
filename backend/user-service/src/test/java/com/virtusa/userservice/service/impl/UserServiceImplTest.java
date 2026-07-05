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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequestDto createRequest;
    private User userEntity;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        createRequest = CreateUserRequestDto.builder()
                .employeeId("EMP999")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@virtusa.com")
                .role(Role.EMPLOYEE)
                .location(Location.HYDERABAD)
                .department("Engineering")
                .build();

        userEntity = User.builder()
                .id(1L)
                .employeeId("EMP999")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@virtusa.com")
                .role(Role.EMPLOYEE)
                .location(Location.HYDERABAD)
                .active(true)
                .build();

        responseDto = UserResponseDto.builder()
                .id(1L)
                .employeeId("EMP999")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@virtusa.com")
                .role(Role.EMPLOYEE)
                .location(Location.HYDERABAD)
                .active(true)
                .build();
    }

    @Test
    void createEmployee_asGlobalHr_success() {
        // Arrange
        when(userRepository.existsByEmployeeId(createRequest.getEmployeeId())).thenReturn(false);
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(createRequest)).thenReturn(userEntity);
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponseDto(userEntity)).thenReturn(responseDto);

        // Act
        UserResponseDto result = userService.createEmployee(createRequest, Role.GLOBAL_HR, Location.SINGAPORE);

        // Assert
        assertNotNull(result);
        assertEquals("EMP999", result.getEmployeeId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createEmployee_asHrSameLocation_success() {
        // Arrange
        when(userRepository.existsByEmployeeId(createRequest.getEmployeeId())).thenReturn(false);
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(createRequest)).thenReturn(userEntity);
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponseDto(userEntity)).thenReturn(responseDto);

        // Act
        UserResponseDto result = userService.createEmployee(createRequest, Role.HR, Location.HYDERABAD);

        // Assert
        assertNotNull(result);
        assertEquals(Location.HYDERABAD, result.getLocation());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createEmployee_asHrDifferentLocation_throwsUnauthorizedException() {
        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> 
                userService.createEmployee(createRequest, Role.HR, Location.LONDON)
        );
        assertTrue(exception.getMessage().contains("HR users can only onboard employees belonging to their location"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createEmployee_asHrCreatingHrRole_throwsUnauthorizedException() {
        // Arrange
        createRequest.setRole(Role.HR);

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> 
                userService.createEmployee(createRequest, Role.HR, Location.HYDERABAD)
        );
        assertEquals("HR users can only onboard users with role EMPLOYEE.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createEmployee_asEmployee_throwsUnauthorizedException() {
        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> 
                userService.createEmployee(createRequest, Role.EMPLOYEE, Location.HYDERABAD)
        );
        assertEquals("EMPLOYEE users are not allowed to create or onboard profiles.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createEmployee_duplicateEmployeeId_throwsDuplicateResourceException() {
        // Arrange
        when(userRepository.existsByEmployeeId(createRequest.getEmployeeId())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> 
                userService.createEmployee(createRequest, Role.GLOBAL_HR, Location.SINGAPORE)
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getEmployeeById_found_success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponseDto(userEntity)).thenReturn(responseDto);

        // Act
        UserResponseDto result = userService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getEmployeeById_notFound_throwsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getEmployeeById(1L));
    }

    @Test
    void updateEmployee_asHrSameLocation_success() {
        // Arrange
        UpdateUserRequestDto updateRequest = UpdateUserRequestDto.builder()
                .firstName("Johnny")
                .role(Role.EMPLOYEE)
                .location(Location.HYDERABAD)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponseDto(userEntity)).thenReturn(responseDto);

        // Act
        UserResponseDto result = userService.updateEmployee(1L, updateRequest, Role.HR, Location.HYDERABAD);

        // Assert
        assertNotNull(result);
        verify(userMapper, times(1)).updateEntity(updateRequest, userEntity);
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    void deactivateEmployee_asHrSameLocation_success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponseDto(userEntity)).thenReturn(responseDto);

        // Act
        UserResponseDto result = userService.deactivateEmployee(1L, Role.HR, Location.HYDERABAD);

        // Assert
        assertNotNull(result);
        assertFalse(userEntity.isActive());
        verify(userRepository, times(1)).save(userEntity);
    }
}
