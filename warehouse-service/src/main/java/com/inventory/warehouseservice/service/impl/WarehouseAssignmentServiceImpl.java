package com.inventory.warehouseservice.service.impl;

import com.inventory.warehouseservice.dto.request.AssignEmployeeRequest;
import com.inventory.warehouseservice.dto.request.UpdateAssignmentRequest;
import com.inventory.warehouseservice.dto.response.InternalEmployeeResponse;
import com.inventory.warehouseservice.dto.response.WarehouseAssignmentResponse;
import com.inventory.warehouseservice.entity.Warehouse;
import com.inventory.warehouseservice.entity.WarehouseAssignment;
import com.inventory.warehouseservice.enums.WarehouseRole;
import com.inventory.warehouseservice.event.KafkaEventPublisher;
import com.inventory.warehouseservice.event.WarehouseAssignmentEvent;
import com.inventory.warehouseservice.exception.InvalidOperationException;
import com.inventory.warehouseservice.exception.ResourceNotFoundException;
import com.inventory.warehouseservice.exception.DuplicateResourceException;
import com.inventory.warehouseservice.feign.AuthFeignClient;
import com.inventory.warehouseservice.repository.WarehouseAssignmentRepository;
import com.inventory.warehouseservice.repository.WarehouseRepository;
import com.inventory.warehouseservice.service.interfaces.WarehouseAssignmentService;
import com.inventory.warehouseservice.util.RoleHierarchyUtil;
import com.inventory.warehouseservice.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WarehouseAssignmentServiceImpl implements WarehouseAssignmentService {

    private final WarehouseAssignmentRepository assignmentRepository;
    private final WarehouseRepository warehouseRepository;
    private final AuthFeignClient authFeignClient;
    private final KafkaEventPublisher eventPublisher;

    public WarehouseAssignmentServiceImpl(WarehouseAssignmentRepository assignmentRepository, WarehouseRepository warehouseRepository, AuthFeignClient authFeignClient, KafkaEventPublisher eventPublisher) {
        this.assignmentRepository = assignmentRepository;
        this.warehouseRepository = warehouseRepository;
        this.authFeignClient = authFeignClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public WarehouseAssignmentResponse assignEmployee(Long warehouseId, AssignEmployeeRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        // Enforce role hierarchy
        WarehouseRole highestRole = SecurityUtil.getHighestRole();
        if (highestRole == null) {
            throw new InvalidOperationException("Unable to determine your role");
        }
        if (!RoleHierarchyUtil.canAssign(highestRole, request.getWarehouseRole())) {
            throw new InvalidOperationException(
                    "You cannot assign the role " + request.getWarehouseRole() + ". "
                    + highestRole + " can only assign roles below it in the hierarchy.");
        }

        // IM can only assign employees to their own warehouse
        if (highestRole == WarehouseRole.INVENTORY_MANAGER) {
            String employeeCode = SecurityUtil.getCurrentEmployeeCode();
            WarehouseAssignment myAssignment = assignmentRepository.findByEmployeeCodeAndActiveTrue(employeeCode)
                    .orElseThrow(() -> new InvalidOperationException("You are not assigned to any warehouse"));
            if (!myAssignment.getWarehouse().getId().equals(warehouseId)) {
                throw new InvalidOperationException("You can only assign employees to your own assigned warehouse");
            }
        }

        // Validate employee exists and is active
        InternalEmployeeResponse employee = authFeignClient.getEmployeeByCode(request.getEmployeeCode()).getData();
        if (employee == null || !"ACTIVE".equals(employee.getStatus())) {
            throw new InvalidOperationException("Employee not found or not active");
        }
        
        request.setEmployeeId(employee.getEmployeeId()); // Set it for downstream logic

        // Check if employee is already assigned to any active warehouse
        if (assignmentRepository.existsByEmployeeIdAndActiveTrue(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee is already assigned to an active warehouse");
        }

        // Check if assigning INVENTORY_MANAGER and warehouse already has an active manager
        if (request.getWarehouseRole() == WarehouseRole.INVENTORY_MANAGER &&
                assignmentRepository.existsByWarehouseIdAndWarehouseRoleAndActiveTrue(warehouseId, WarehouseRole.INVENTORY_MANAGER)) {
            throw new DuplicateResourceException("Warehouse already has an active manager");
        }

        WarehouseAssignment assignment = new WarehouseAssignment();
        assignment.setWarehouse(warehouse);
        assignment.setEmployeeId(request.getEmployeeId());
        assignment.setEmployeeCode(request.getEmployeeCode());
        assignment.setWarehouseRole(request.getWarehouseRole());
        assignment.setStatus("ACTIVE");
        assignment.setActive(true);
        assignment.setAssignedBy(SecurityUtil.getCurrentEmployeeCode());
        assignment.setUpdatedBy(SecurityUtil.getCurrentEmployeeCode());

        assignment = assignmentRepository.save(assignment);

        // Publish event
        WarehouseAssignmentEvent event = new WarehouseAssignmentEvent(
                "WarehouseEmployeeAssigned",
                warehouseId,
                request.getEmployeeId(),
                request.getWarehouseRole().name()
        );
        event.setPerformedBy(SecurityUtil.getCurrentEmployeeCode());
        eventPublisher.publishAssignmentEvent(event);

        return mapToResponse(assignment);
    }

    @Override
    public Page<WarehouseAssignmentResponse> getAssignmentsByWarehouse(Long warehouseId, Pageable pageable) {
        return assignmentRepository.findByWarehouseId(warehouseId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<WarehouseAssignmentResponse> getActiveAssignmentsByWarehouse(Long warehouseId) {
        return assignmentRepository.findByWarehouseIdAndActiveTrue(warehouseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WarehouseAssignmentResponse updateAssignment(Long assignmentId, UpdateAssignmentRequest request) {
        WarehouseAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!assignment.getActive()) {
            throw new InvalidOperationException("Cannot update an inactive assignment");
        }

        // Enforce role hierarchy
        WarehouseRole highestRole = SecurityUtil.getHighestRole();
        if (highestRole == null) {
            throw new InvalidOperationException("Unable to determine your role");
        }
        if (!RoleHierarchyUtil.canAssign(highestRole, request.getWarehouseRole())) {
            throw new InvalidOperationException(
                    "You cannot assign the role " + request.getWarehouseRole() + ". "
                    + highestRole + " can only assign roles below it in the hierarchy.");
        }

        // IM can only update assignments in their own warehouse
        if (highestRole == WarehouseRole.INVENTORY_MANAGER) {
            String employeeCode = SecurityUtil.getCurrentEmployeeCode();
            WarehouseAssignment myAssignment = assignmentRepository.findByEmployeeCodeAndActiveTrue(employeeCode)
                    .orElseThrow(() -> new InvalidOperationException("You are not assigned to any warehouse"));
            if (!myAssignment.getWarehouse().getId().equals(assignment.getWarehouse().getId())) {
                throw new InvalidOperationException("You can only update assignments in your own assigned warehouse");
            }
        }

        assignment.setWarehouseRole(request.getWarehouseRole());
        assignment.setUpdatedBy(SecurityUtil.getCurrentEmployeeCode());

        assignment = assignmentRepository.save(assignment);

        // Publish event
        WarehouseAssignmentEvent event = new WarehouseAssignmentEvent(
                "WarehouseAssignmentUpdated",
                assignment.getWarehouse().getId(),
                assignment.getEmployeeId(),
                assignment.getWarehouseRole().name()
        );
        event.setPerformedBy(SecurityUtil.getCurrentEmployeeCode());
        eventPublisher.publishAssignmentEvent(event);

        return mapToResponse(assignment);
    }

    @Override
    @Transactional
    public void deactivateAssignment(Long assignmentId) {
        WarehouseAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!assignment.getActive()) {
            throw new InvalidOperationException("Assignment is already inactive");
        }

        // IM can only deactivate assignments in their own warehouse
        WarehouseRole highestRole = SecurityUtil.getHighestRole();
        if (highestRole == WarehouseRole.INVENTORY_MANAGER) {
            String employeeCode = SecurityUtil.getCurrentEmployeeCode();
            WarehouseAssignment myAssignment = assignmentRepository.findByEmployeeCodeAndActiveTrue(employeeCode)
                    .orElseThrow(() -> new InvalidOperationException("You are not assigned to any warehouse"));
            if (!myAssignment.getWarehouse().getId().equals(assignment.getWarehouse().getId())) {
                throw new InvalidOperationException("You can only manage assignments in your own assigned warehouse");
            }
        }

        assignment.setActive(false);
        assignment.setStatus("INACTIVE");
        assignment.setUpdatedBy(SecurityUtil.getCurrentEmployeeCode());
        assignmentRepository.save(assignment);

        // Publish event
        WarehouseAssignmentEvent event = new WarehouseAssignmentEvent(
                "WarehouseEmployeeRemoved",
                assignment.getWarehouse().getId(),
                assignment.getEmployeeId(),
                assignment.getWarehouseRole().name()
        );
        event.setPerformedBy(SecurityUtil.getCurrentEmployeeCode());
        eventPublisher.publishAssignmentEvent(event);
    }

    @Override
    public List<InternalEmployeeResponse> getUnassignedEmployees() {
        List<InternalEmployeeResponse> allEmployees;
        try {
            var response = authFeignClient.getAllEmployees();
            allEmployees = (response != null && response.getData() != null) ? response.getData() : List.of();
        } catch (Exception e) {
            return List.of();
        }

        Set<Long> assignedEmployeeIds = assignmentRepository.findByActiveTrue().stream()
                .map(WarehouseAssignment::getEmployeeId)
                .collect(Collectors.toSet());

        return allEmployees.stream()
                .filter(emp -> "ACTIVE".equals(emp.getStatus()))
                .filter(emp -> !assignedEmployeeIds.contains(emp.getEmployeeId()))
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseAssignmentResponse getMyAssignment() {
        String employeeCode = SecurityUtil.getCurrentEmployeeCode();
        WarehouseAssignment assignment = assignmentRepository.findByEmployeeCodeAndActiveTrue(employeeCode)
                .orElse(null);
        if (assignment == null) {
            return null;
        }
        return mapToResponse(assignment);
    }

    private WarehouseAssignmentResponse mapToResponse(WarehouseAssignment assignment) {
        WarehouseAssignmentResponse response = new WarehouseAssignmentResponse();
        response.setAssignmentId(assignment.getId());
        response.setWarehouseId(assignment.getWarehouse().getId());
        response.setWarehouseCode(assignment.getWarehouse().getWarehouseCode());
        response.setEmployeeId(assignment.getEmployeeId());
        response.setEmployeeCode(assignment.getEmployeeCode());
        response.setWarehouseRole(assignment.getWarehouseRole());
        response.setStatus(assignment.getStatus());
        response.setAssignedBy(assignment.getAssignedBy());
        response.setAssignedAt(assignment.getAssignedAt());
        response.setUpdatedAt(assignment.getUpdatedAt());
        return response;
    }
}
