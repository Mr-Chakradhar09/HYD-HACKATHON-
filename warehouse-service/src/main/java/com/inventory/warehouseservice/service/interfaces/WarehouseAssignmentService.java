package com.inventory.warehouseservice.service.interfaces;

import com.inventory.warehouseservice.dto.request.AssignEmployeeRequest;
import com.inventory.warehouseservice.dto.request.UpdateAssignmentRequest;
import com.inventory.warehouseservice.dto.response.InternalEmployeeResponse;
import com.inventory.warehouseservice.dto.response.WarehouseAssignmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarehouseAssignmentService {

    WarehouseAssignmentResponse assignEmployee(Long warehouseId, AssignEmployeeRequest request);

    Page<WarehouseAssignmentResponse> getAssignmentsByWarehouse(Long warehouseId, Pageable pageable);

    List<WarehouseAssignmentResponse> getActiveAssignmentsByWarehouse(Long warehouseId);

    WarehouseAssignmentResponse updateAssignment(Long assignmentId, UpdateAssignmentRequest request);

    void deactivateAssignment(Long assignmentId);

    List<InternalEmployeeResponse> getUnassignedEmployees();

    WarehouseAssignmentResponse getMyAssignment();
}
