package com.inventory.authservice.service.impl;

import com.inventory.authservice.dto.request.CreateEmployeeRequest;
import com.inventory.authservice.dto.request.UpdateEmployeeRequest;
import com.inventory.authservice.dto.response.CreateEmployeeResponse;
import com.inventory.authservice.dto.response.EmployeeResponse;
import com.inventory.authservice.dto.response.InternalEmployeeResponse;
import com.inventory.authservice.entity.Employee;
import com.inventory.authservice.entity.Role;
import com.inventory.authservice.enums.EmployeeStatus;
import com.inventory.authservice.enums.RoleType;
import com.inventory.authservice.exception.EmployeeNotFoundException;
import com.inventory.authservice.exception.RoleNotFoundException;
import com.inventory.authservice.exception.UserAlreadyExistsException;
import com.inventory.authservice.repository.EmployeeRepository;
import com.inventory.authservice.repository.RoleRepository;
import com.inventory.authservice.security.RoleHierarchyUtil;
import com.inventory.authservice.service.EmployeeService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, RoleRepository roleRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
    }
    @Override
    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RoleType creatorRole = authentication.getAuthorities()
                        .stream()
                        .map(authority -> authority.getAuthority().replace("ROLE_", "")).map(RoleType::valueOf).findFirst().orElseThrow(() -> new RuntimeException("Role not found"));
        if(request.getRoles().stream().anyMatch(role -> !RoleHierarchyUtil.canAssign(creatorRole, role))) {
            throw new AccessDeniedException("You cannot assign one or more roles");
        }
        if(employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new UserAlreadyExistsException("Employee code already exists");
        }
        if(employeeRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        Set<Role> roles = request.getRoles().stream().map(role -> roleRepository.findByRoleName(role).orElseThrow(() -> new RuntimeException("Role not found"))).collect(Collectors.toSet());
        Employee employee = new Employee();
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setRoles(roles);
        employee.setStatus(EmployeeStatus.PENDING_REGISTRATION);
        employee.setCreatedBy(authentication.getName());
        employeeRepository.save(employee);
        CreateEmployeeResponse response = new CreateEmployeeResponse();
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFullName(employee.getFullName());
        response.setEmail(employee.getEmail());
        response.setRoles(roles.stream().map(role -> role.getRoleName().name()).toList());
        response.setStatus(employee.getStatus());
        response.setMessage("Employee created successfully");
        return response;
    }
    @Override
    public EmployeeResponse getEmployeeByCode(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + employeeCode));
        return mapToEmployeeResponse(employee);
    }

    @Transactional(readOnly = true)
    @Override
    public InternalEmployeeResponse getInternalEmployeeById(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeNotFoundException("Employee not found "));
        Set<String> roles = employee.getRoles().stream().map(role -> role.getRoleName().name()).collect(Collectors.toSet());
        return new InternalEmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getStatus(),
                roles
        );
    }

    @Transactional(readOnly = true)
    @Override
    public InternalEmployeeResponse getInternalEmployeeByCode(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElseThrow(() -> new EmployeeNotFoundException("Employee not found "));
        Set<String> roles = employee.getRoles().stream().map(role -> role.getRoleName().name()).collect(Collectors.toSet());
        return new InternalEmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getStatus(),
                roles
        );
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::mapToEmployeeResponse).toList();
    }
    @Override
    @Transactional
    public EmployeeResponse updateEmployee(String employeeCode, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElseThrow(() ->
                                new EmployeeNotFoundException("Employee not found"));
        if (!employee.getEmail().equals(request.getEmail()) && employeeRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        Set<Role> roles = request.getRoles()
                .stream()
                .map(roleType -> roleRepository.findByRoleName(roleType)
                        .orElseThrow(() ->
                                new RoleNotFoundException("Role not found: " + roleType)))
                .collect(Collectors.toSet());
        employee.setRoles(roles);
        employeeRepository.save(employee);
        return mapToEmployeeResponse(employee);
    }
    @Override
    @Transactional
    public void activateEmployee(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        if(employee.getStatus() == EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("Employee is already active");
        }
        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);
    }
    @Override
    @Transactional
    public void deactivateEmployee(String employeeCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmployeeCode = authentication.getName();
        if(loggedInEmployeeCode.equals(employeeCode)) {
            throw new IllegalStateException("You cannot deactivate your own account");
        }
        Employee employee =employeeRepository.findByEmployeeCode(employeeCode).orElseThrow(() ->
                                new EmployeeNotFoundException("Employee not found"));
        if(employee.getStatus() == EmployeeStatus.INACTIVE) {
            throw new IllegalStateException("Employee is already inactive");
        }
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    }
    @Override
    @Transactional
    public void deleteEmployee(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElseThrow(() ->
                                new EmployeeNotFoundException("Employee not found"));
        if(employee.getStatus() != EmployeeStatus.PENDING_REGISTRATION) {
            throw new IllegalStateException("Only pending employees can be deleted");
        }
        employeeRepository.delete(employee);
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFullName(employee.getFullName());
        response.setEmail(employee.getEmail());
        response.setRoles(employee.getRoles().stream().map(role -> role.getRoleName().name()).toList());
        response.setStatus(employee.getStatus());
        return response;
    }
}