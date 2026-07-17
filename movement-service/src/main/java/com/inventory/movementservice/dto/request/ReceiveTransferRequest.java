package com.inventory.movementservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReceiveTransferRequest {

    @NotNull(message = "Received quantity is required")
    @Min(value = 1, message = "Received quantity must be at least 1")
    private Integer receivedQuantity;

    private String remarks;

    public ReceiveTransferRequest() {}

    public Integer getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
