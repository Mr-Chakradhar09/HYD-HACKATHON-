package com.inventory.warehouseservice.entity;

import com.inventory.warehouseservice.enums.WarehouseRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_assignments")
public class WarehouseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String employeeCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private WarehouseRole warehouseRole;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String assignedBy;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    @Version
    private Long version;

    private Boolean active = true;

    public WarehouseAssignment() {
    }

    @PrePersist
    public void prePersist() {
        this.assignedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}