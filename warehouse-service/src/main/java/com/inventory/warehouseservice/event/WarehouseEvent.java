package com.inventory.warehouseservice.event;

import java.time.LocalDateTime;

public class WarehouseEvent {

    private String eventId;
    private String eventType;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String performedBy;
    private LocalDateTime timestamp;
    private int eventVersion;

    public WarehouseEvent() {
        this.timestamp = LocalDateTime.now();
        this.eventVersion = 1;
    }

    public WarehouseEvent(String eventType, Long warehouseId, String warehouseCode) {
        this();
        this.eventId = "WH_EVENT_" + System.currentTimeMillis();
        this.eventType = eventType;
        this.warehouseId = warehouseId;
        this.warehouseCode = warehouseCode;
    }

    public WarehouseEvent(String eventType, Long warehouseId, String warehouseCode, String performedBy) {
        this();
        this.eventId = "WH_EVENT_" + System.currentTimeMillis();
        this.eventType = eventType;
        this.warehouseId = warehouseId;
        this.warehouseCode = warehouseCode;
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
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }
}
