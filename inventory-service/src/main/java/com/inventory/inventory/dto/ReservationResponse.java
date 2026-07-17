package com.inventory.inventory.dto;

import com.inventory.inventory.entity.Reservation;
import com.inventory.inventory.enums.ReservationStatus;
import java.time.LocalDateTime;

public class ReservationResponse {
    private Long id;
    private String orderId;
    private Long productId;
    private Long warehouseId;
    private Integer quantity;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReservationResponse fromEntity(Reservation r) {
        ReservationResponse resp = new ReservationResponse();
        resp.id = r.getId();
        resp.orderId = r.getOrderId();
        resp.productId = r.getProductId();
        resp.warehouseId = r.getWarehouseId();
        resp.quantity = r.getQuantity();
        resp.status = r.getStatus();
        resp.createdAt = r.getCreatedAt();
        resp.updatedAt = r.getUpdatedAt();
        return resp;
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public Long getProductId() { return productId; }
    public Long getWarehouseId() { return warehouseId; }
    public Integer getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
