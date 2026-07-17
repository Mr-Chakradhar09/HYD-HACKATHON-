package com.inventory.replenishment.dto;

import com.inventory.replenishment.entity.PurchaseRequest;
import com.inventory.replenishment.enums.PurchaseRequestStatus;
import java.time.LocalDateTime;

public class PurchaseRequestResponse {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private Integer requiredQuantity;
    private PurchaseRequestStatus status;
    private LocalDateTime createdAt;

    public static PurchaseRequestResponse fromEntity(PurchaseRequest pr) {
        PurchaseRequestResponse r = new PurchaseRequestResponse();
        r.id = pr.getId(); r.productId = pr.getProductId();
        r.warehouseId = pr.getWarehouseId(); r.requiredQuantity = pr.getRequiredQuantity();
        r.status = pr.getStatus(); r.createdAt = pr.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getWarehouseId() { return warehouseId; }
    public Integer getRequiredQuantity() { return requiredQuantity; }
    public PurchaseRequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
