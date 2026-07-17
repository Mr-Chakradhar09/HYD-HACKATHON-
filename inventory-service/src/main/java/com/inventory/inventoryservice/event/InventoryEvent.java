package com.inventory.inventoryservice.event;

import java.time.LocalDateTime;

public class InventoryEvent {
    private String eventId;
    private String eventType;
    private Long inventoryId;
    private Long productId;
    private Long warehouseId;
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer changeQuantity;
    private String movementNumber;
    private String performedBy;
    private LocalDateTime timestamp;
    private int eventVersion;

    public InventoryEvent() { this.timestamp = LocalDateTime.now(); this.eventVersion = 1; }

    public InventoryEvent(String eventType, Long inventoryId, Long productId, Long warehouseId) {
        this();
        this.eventId = "INV_EVENT_" + System.currentTimeMillis();
        this.eventType = eventType;
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.warehouseId = warehouseId;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Integer getPreviousQuantity() { return previousQuantity; }
    public void setPreviousQuantity(Integer previousQuantity) { this.previousQuantity = previousQuantity; }
    public Integer getNewQuantity() { return newQuantity; }
    public void setNewQuantity(Integer newQuantity) { this.newQuantity = newQuantity; }
    public Integer getChangeQuantity() { return changeQuantity; }
    public void setChangeQuantity(Integer changeQuantity) { this.changeQuantity = changeQuantity; }
    public String getMovementNumber() { return movementNumber; }
    public void setMovementNumber(String movementNumber) { this.movementNumber = movementNumber; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }
}
