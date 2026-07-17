package com.inventory.inventoryservice.dto.request;

import com.inventory.inventoryservice.enums.AdjustmentType;
import jakarta.validation.constraints.*;

public class CreateAdjustmentRequest {

    @NotNull(message = "Inventory ID is required")
    private Long inventoryId;

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType adjustmentType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String remarks;

    public CreateAdjustmentRequest() {}

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public AdjustmentType getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(AdjustmentType adjustmentType) { this.adjustmentType = adjustmentType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
