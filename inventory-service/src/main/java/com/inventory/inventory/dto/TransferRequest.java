package com.inventory.inventory.dto;

import jakarta.validation.constraints.NotNull;

public class TransferRequest {
    @NotNull private Long productId;
    @NotNull private Long fromWarehouseId;
    @NotNull private Long toWarehouseId;
    @NotNull private Integer quantity;
    private String reference;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(Long fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }
    public Long getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(Long toWarehouseId) { this.toWarehouseId = toWarehouseId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
