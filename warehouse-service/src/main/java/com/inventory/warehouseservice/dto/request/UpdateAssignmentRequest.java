package com.inventory.warehouseservice.dto.request;

import com.inventory.warehouseservice.enums.WarehouseRole;
import jakarta.validation.constraints.NotNull;

public class UpdateAssignmentRequest {

    @NotNull(message = "Warehouse role is required")
    private WarehouseRole warehouseRole;

    public UpdateAssignmentRequest() {
    }

    public WarehouseRole getWarehouseRole() {
        return warehouseRole;
    }

    public void setWarehouseRole(WarehouseRole warehouseRole) {
        this.warehouseRole = warehouseRole;
    }
}
