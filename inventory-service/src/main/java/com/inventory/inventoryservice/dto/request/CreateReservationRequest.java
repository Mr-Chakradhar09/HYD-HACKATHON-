package com.inventory.inventoryservice.dto.request;

import jakarta.validation.constraints.*;

public class CreateReservationRequest {

    @NotNull(message = "Inventory ID is required")
    private Long inventoryId;

    @NotBlank(message = "Reference number is required")
    private String referenceNumber;

    @NotBlank(message = "Reference type is required")
    private String referenceType;

    @NotNull(message = "Reserved quantity is required")
    @Min(value = 1, message = "Reserved quantity must be greater than 0")
    private Integer reservedQuantity;

    public CreateReservationRequest() {}

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
}
