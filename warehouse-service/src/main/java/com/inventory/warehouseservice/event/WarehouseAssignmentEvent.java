package com.inventory.warehouseservice.event;

import java.time.LocalDateTime;

public class WarehouseAssignmentEvent {

    private String eventId;
    private String eventType;
    private Long warehouseId;
    private String warehouseCode;
    private Long employeeId;
    private String employeeCode;
    private String warehouseRole;
    private String performedBy;
    private LocalDateTime timestamp;
    private int eventVersion;

    public WarehouseAssignmentEvent() {
        this.timestamp = LocalDateTime.now();
        this.eventVersion = 1;
    }

    public WarehouseAssignmentEvent(String eventType, Long warehouseId, Long employeeId, String warehouseRole) {
        this();
        this.eventId = "WHA_EVENT_" + System.currentTimeMillis();
        this.eventType = eventType;
        this.warehouseId = warehouseId;
        this.employeeId = employeeId;
        this.warehouseRole = warehouseRole;
    }

    public WarehouseAssignmentEvent(String eventType, Long warehouseId, String warehouseCode,
                                     Long employeeId, String employeeCode, String warehouseRole,
                                     String performedBy) {
        this();
        this.eventId = "WHA_EVENT_" + System.currentTimeMillis();
        this.eventType = eventType;
        this.warehouseId = warehouseId;
        this.warehouseCode = warehouseCode;
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.warehouseRole = warehouseRole;
        this.performedBy = performedBy;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getWarehouseRole() { return warehouseRole; }
    public void setWarehouseRole(String warehouseRole) { this.warehouseRole = warehouseRole; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }
}
