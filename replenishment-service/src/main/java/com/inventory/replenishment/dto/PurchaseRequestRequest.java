package com.inventory.replenishment.dto;

import jakarta.validation.constraints.NotNull;

public class PurchaseRequestRequest {
    @NotNull private Long productId;
    @NotNull private Long warehouseId;
    @NotNull private Integer requiredQuantity;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Integer getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(Integer requiredQuantity) { this.requiredQuantity = requiredQuantity; }
}
