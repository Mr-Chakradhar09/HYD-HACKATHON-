package com.inventory.movementservice.dto.request;

public class RejectTransferRequest {

    private String rejectionReason;

    public RejectTransferRequest() {}

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
