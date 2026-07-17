package com.inventory.movementservice.event;

import java.time.LocalDateTime;

public class MovementEvent {
    private String eventId;
    private String eventType;
    private String movementId;
    private String movementNumber;
    private String movementType;
    private Long productId;
    private Long sourceWarehouseId;
    private Long destinationWarehouseId;
    private Integer quantity;
    private String performedBy;
    private LocalDateTime timestamp;
    private int eventVersion;

    public MovementEvent() { this.timestamp = LocalDateTime.now(); this.eventVersion = 1; }

    public MovementEvent(String eventType, String movementId, String movementNumber) {
        this();
        this.eventId = "MOV_EVENT_" + System.currentTimeMillis();
        this.eventType = eventType;
        this.movementId = movementId;
        this.movementNumber = movementNumber;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getMovementId() { return movementId; }
    public void setMovementId(String movementId) { this.movementId = movementId; }
    public String getMovementNumber() { return movementNumber; }
    public void setMovementNumber(String movementNumber) { this.movementNumber = movementNumber; }
    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getSourceWarehouseId() { return sourceWarehouseId; }
    public void setSourceWarehouseId(Long sourceWarehouseId) { this.sourceWarehouseId = sourceWarehouseId; }
    public Long getDestinationWarehouseId() { return destinationWarehouseId; }
    public void setDestinationWarehouseId(Long destinationWarehouseId) { this.destinationWarehouseId = destinationWarehouseId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getEventVersion() { return eventVersion; }
}
