package com.inventory.reportingservice.dto.response;

import java.time.LocalDateTime;

public class MovementHistoryResponse {
    private Long id;
    private String eventId;
    private String movementNumber;
    private String movementType;
    private Long productId;
    private Long sourceWarehouseId;
    private Long destinationWarehouseId;
    private Integer quantity;
    private String performedBy;
    private LocalDateTime movementTimestamp;

    public MovementHistoryResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
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
    public LocalDateTime getMovementTimestamp() { return movementTimestamp; }
    public void setMovementTimestamp(LocalDateTime movementTimestamp) { this.movementTimestamp = movementTimestamp; }
}
