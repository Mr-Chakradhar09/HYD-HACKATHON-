package com.inventory.movementservice.entity;

import com.inventory.movementservice.enums.TransferRequestStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_requests")
public class TransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_number", unique = true, nullable = false)
    private String requestNumber;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "source_warehouse_id", nullable = false)
    private Long sourceWarehouseId;

    @Column(name = "destination_warehouse_id", nullable = false)
    private Long destinationWarehouseId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferRequestStatus status;

    private String reason;

    private String remarks;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "movement_id")
    private Long movementId;

    @Column(name = "dispatched_quantity")
    private Integer dispatchedQuantity;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "dispatched_by")
    private String dispatchedBy;

    @Column(name = "received_quantity")
    private Integer receivedQuantity;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(name = "dispatch_movement_id")
    private Long dispatchMovementId;

    @Column(name = "receive_movement_id")
    private Long receiveMovementId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = TransferRequestStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getSourceWarehouseId() { return sourceWarehouseId; }
    public void setSourceWarehouseId(Long sourceWarehouseId) { this.sourceWarehouseId = sourceWarehouseId; }
    public Long getDestinationWarehouseId() { return destinationWarehouseId; }
    public void setDestinationWarehouseId(Long destinationWarehouseId) { this.destinationWarehouseId = destinationWarehouseId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public TransferRequestStatus getStatus() { return status; }
    public void setStatus(TransferRequestStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Long getMovementId() { return movementId; }
    public void setMovementId(Long movementId) { this.movementId = movementId; }
    public Integer getDispatchedQuantity() { return dispatchedQuantity; }
    public void setDispatchedQuantity(Integer dispatchedQuantity) { this.dispatchedQuantity = dispatchedQuantity; }
    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }
    public String getDispatchedBy() { return dispatchedBy; }
    public void setDispatchedBy(String dispatchedBy) { this.dispatchedBy = dispatchedBy; }
    public Integer getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public Long getDispatchMovementId() { return dispatchMovementId; }
    public void setDispatchMovementId(Long dispatchMovementId) { this.dispatchMovementId = dispatchMovementId; }
    public Long getReceiveMovementId() { return receiveMovementId; }
    public void setReceiveMovementId(Long receiveMovementId) { this.receiveMovementId = receiveMovementId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
