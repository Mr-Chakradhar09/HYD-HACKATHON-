package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.request.CreateAdjustmentRequest;
import com.inventory.inventoryservice.entity.InventoryAdjustment;

public interface AdjustmentService {
    InventoryAdjustment createAdjustment(CreateAdjustmentRequest request, String adjustedBy);
}
