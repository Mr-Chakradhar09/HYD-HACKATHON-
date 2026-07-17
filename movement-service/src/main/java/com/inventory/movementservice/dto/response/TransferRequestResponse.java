package com.inventory.movementservice.dto.response;

import com.inventory.movementservice.enums.TransferRequestStatus;

import java.time.LocalDateTime;

public class TransferRequestResponse {
    private Long id;
    private String requestNumber;
    private Long productId;
    private Long sourceWarehouseId;
    private Long destinationWarehouseId;
    private Integer quantity;
    private TransferRequestStatus status;
    private String reason;
    private String remarks;
    private String requestedBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private Long movementId;
    private Integer dispatchedQuantity;
    private LocalDateTime dispatchedAt;
    private String dispatchedBy;
    private Integer receivedQuantity;
    private LocalDateTime receivedAt;
    private String receivedBy;
    private Long dispatchMovementId;
    private Long receiveMovementId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TransferRequestResponse() {}

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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
