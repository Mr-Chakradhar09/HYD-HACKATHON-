package com.inventory.inventory.dto;

import com.inventory.inventory.entity.Inventory;
import java.time.LocalDateTime;

public class InventoryResponse {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private Integer quantity;
    private Integer reserved;
    private Integer available;
    private LocalDateTime lastUpdated;

    public static InventoryResponse fromEntity(Inventory inv) {
        InventoryResponse r = new InventoryResponse();
        r.id = inv.getId(); r.productId = inv.getProductId();
        r.warehouseId = inv.getWarehouseId(); r.quantity = inv.getQuantity();
        r.reserved = inv.getReserved(); r.available = inv.getAvailable();
        r.lastUpdated = inv.getLastUpdated();
        return r;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getWarehouseId() { return warehouseId; }
    public Integer getQuantity() { return quantity; }
    public Integer getReserved() { return reserved; }
    public Integer getAvailable() { return available; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
