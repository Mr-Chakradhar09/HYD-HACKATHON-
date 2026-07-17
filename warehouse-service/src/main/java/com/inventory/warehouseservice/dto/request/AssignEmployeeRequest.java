package com.inventory.warehouseservice.dto.request;

import com.inventory.warehouseservice.enums.WarehouseRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AssignEmployeeRequest {

    private Long employeeId;

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotNull(message = "Warehouse role is required")
    private WarehouseRole warehouseRole;

    private Long assignedBy;

    public AssignEmployeeRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public WarehouseRole getWarehouseRole() { return warehouseRole; }
    public void setWarehouseRole(WarehouseRole warehouseRole) { this.warehouseRole = warehouseRole; }
    public Long getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Long assignedBy) { this.assignedBy = assignedBy; }
}
