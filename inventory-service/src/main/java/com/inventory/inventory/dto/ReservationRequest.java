package com.inventory.inventory.dto;

import jakarta.validation.constraints.NotNull;

public class ReservationRequest {
    @NotNull private String orderId;
    @NotNull private Long productId;
    @NotNull private Long warehouseId;
    @NotNull private Integer quantity;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
