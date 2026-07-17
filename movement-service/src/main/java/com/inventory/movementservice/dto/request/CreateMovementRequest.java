package com.inventory.movementservice.dto.request;

import com.inventory.movementservice.enums.MovementType;
import com.inventory.movementservice.enums.ReferenceType;
import jakarta.validation.constraints.*;

public class CreateMovementRequest {

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long sourceWarehouseId;

    private Long destinationWarehouseId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Reference type is required")
    private ReferenceType referenceType;

    private String referenceNumber;

    private String remarks;

    private String batchNumber;

    private String correlationId;

    public CreateMovementRequest() {}

    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getSourceWarehouseId() { return sourceWarehouseId; }
    public void setSourceWarehouseId(Long sourceWarehouseId) { this.sourceWarehouseId = sourceWarehouseId; }
    public Long getDestinationWarehouseId() { return destinationWarehouseId; }
    public void setDestinationWarehouseId(Long destinationWarehouseId) { this.destinationWarehouseId = destinationWarehouseId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public ReferenceType getReferenceType() { return referenceType; }
    public void setReferenceType(ReferenceType referenceType) { this.referenceType = referenceType; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
