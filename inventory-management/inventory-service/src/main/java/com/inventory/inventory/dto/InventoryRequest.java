package com.inventory.inventory.dto;

import jakarta.validation.constraints.NotNull;

public class InventoryRequest {
    @NotNull private Long productId;
    @NotNull private Long warehouseId;
    @NotNull private Integer quantity;
    private String reference;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
