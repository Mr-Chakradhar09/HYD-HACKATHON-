package com.inventory.movementservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DispatchTransferRequest {

    @NotNull(message = "Dispatched quantity is required")
    @Min(value = 1, message = "Dispatched quantity must be at least 1")
    private Integer dispatchedQuantity;

    private String remarks;

    public DispatchTransferRequest() {}

    public Integer getDispatchedQuantity() { return dispatchedQuantity; }
    public void setDispatchedQuantity(Integer dispatchedQuantity) { this.dispatchedQuantity = dispatchedQuantity; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
