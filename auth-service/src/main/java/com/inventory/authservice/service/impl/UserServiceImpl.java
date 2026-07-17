package com.inventory.authservice.service.impl;

import com.inventory.authservice.dto.request.ChangePasswordRequest;
import com.inventory.authservice.dto.request.CreateEmployeeRequest;
import com.inventory.authservice.dto.request.RegisterRequest;
import com.inventory.authservice.dto.response.CreateEmployeeResponse;
import com.inventory.authservice.dto.response.InternalEmployeeResponse;
import com.inventory.authservice.dto.response.UserResponse;
import com.inventory.authservice.entity.Employee;
import com.inventory.authservice.entity.Role;
import com.inventory.authservice.entity.User;
import com.inventory.authservice.enums.EmployeeStatus;
import com.inventory.authservice.exception.*;
import com.inventory.authservice.repository.EmployeeRepository;
import com.inventory.authservice.repository.RoleRepository;
import com.inventory.authservice.repository.UserRepository;
import com.inventory.authservice.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    //private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, EmployeeRepository employeeRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                        .orElseThrow(() -> new UserNotFoundException("Employee not found"));
        if (userRepository.existsByEmployee_EmployeeCode(request.getEmployeeCode())) {
            throw new UserAlreadyExistsException("Account already exists");
        }
        User user = new User();
        user.setUsername(request.getEmployeeCode());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setEmployee(employee);
        employee.setFullName(request.getFullName());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);
        userRepository.save(user);
        return mapToResponse(user);
    }
    @Override
    public UserResponse getCurrentUser() {
        User user = getAuthenticatedUser();
        return mapToResponse(user);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeCode = authentication.getName();
        return userRepository.findByEmployee_EmployeeCode(employeeCode).orElseThrow(() -> new UserNotFoundException("Employee not found"));
    }
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }
    @Override
    public UserResponse findByEmployeeCode(String employeeCode) {
        User user = userRepository.findByEmployee_EmployeeCode(employeeCode)
                .orElseThrow(() -> new UserNotFoundException("Employee not found with code: " + employeeCode));
        return mapToResponse(user);
    }

    @Override
    public InternalEmployeeResponse getInternalEmployeeById(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Employee not found with id: " + employeeId));
        InternalEmployeeResponse response = new InternalEmployeeResponse();
        response.setEmployeeId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFullName(employee.getFullName());
        response.setEmail(employee.getEmail());
        response.setStatus(employee.getStatus());
        response.setRoles(
                employee.getRoles().stream()
                        .map(role -> role.getRoleName().name())
                        .collect(Collectors.toSet())
        );
        return response;
    }
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).toList();
    }
    @Override
    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEnabled(true);
        Employee employee = user.getEmployee();
        employee.setStatus(EmployeeStatus.ACTIVE);
        userRepository.save(user);
        employeeRepository.save(employee);
    }
    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEnabled(false);
        Employee employee = user.getEmployee();
        employee.setStatus(EmployeeStatus.INACTIVE);
        userRepository.save(user);
        employeeRepository.save(employee);
    }
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeCode = authentication.getName();
        User user = userRepository.findByEmployee_EmployeeCode(employeeCode).orElseThrow(() ->
                        new UserNotFoundException("Employee not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password cannot be same as old password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmployeeCode(user.getEmployee().getEmployeeCode());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmployee().getEmail());
        response.setEnabled(user.getEnabled());
        response.setRoles(user.getEmployee().getRoles()
                        .stream()
                        .map(role -> role.getRoleName().name())
                        .toList());
        return response;
    }

}
