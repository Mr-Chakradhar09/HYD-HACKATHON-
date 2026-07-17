package com.inventory.inventoryservice.dto.response;

import com.inventory.inventoryservice.enums.InventoryStatus;
import java.time.LocalDateTime;

public class InventoryResponse {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private Integer currentQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer damagedQuantity;
    private InventoryStatus status;
    private LocalDateTime lastMovementAt;
    private Long version;

    public InventoryResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Integer getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(Integer currentQuantity) { this.currentQuantity = currentQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    public Integer getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(Integer damagedQuantity) { this.damagedQuantity = damagedQuantity; }
    public InventoryStatus getStatus() { return status; }
    public void setStatus(InventoryStatus status) { this.status = status; }
    public LocalDateTime getLastMovementAt() { return lastMovementAt; }
    public void setLastMovementAt(LocalDateTime lastMovementAt) { this.lastMovementAt = lastMovementAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
