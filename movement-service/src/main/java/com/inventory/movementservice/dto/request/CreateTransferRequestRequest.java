package com.inventory.movementservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateTransferRequestRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Source warehouse ID is required")
    private Long sourceWarehouseId;

    @NotNull(message = "Destination warehouse ID is required")
    private Long destinationWarehouseId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    private String reason;

    private String remarks;

    public CreateTransferRequestRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getSourceWarehouseId() { return sourceWarehouseId; }
    public void setSourceWarehouseId(Long sourceWarehouseId) { this.sourceWarehouseId = sourceWarehouseId; }
    public Long getDestinationWarehouseId() { return destinationWarehouseId; }
    public void setDestinationWarehouseId(Long destinationWarehouseId) { this.destinationWarehouseId = destinationWarehouseId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
