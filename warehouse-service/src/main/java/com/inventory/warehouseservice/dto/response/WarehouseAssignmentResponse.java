package com.inventory.warehouseservice.dto.response;

import com.inventory.warehouseservice.enums.WarehouseRole;

import java.time.LocalDateTime;

public class WarehouseAssignmentResponse {

    private Long assignmentId;
    private Long warehouseId;
    private String warehouseCode;
    private Long employeeId;
    private String employeeCode;
    private WarehouseRole warehouseRole;
    private String status;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;

    public WarehouseAssignmentResponse() {
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public WarehouseRole getWarehouseRole() {
        return warehouseRole;
    }

    public void setWarehouseRole(WarehouseRole warehouseRole) {
        this.warehouseRole = warehouseRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}