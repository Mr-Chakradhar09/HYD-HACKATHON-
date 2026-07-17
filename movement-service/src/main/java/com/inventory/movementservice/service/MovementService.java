package com.inventory.movementservice.service;

import com.inventory.movementservice.dto.request.CreateMovementRequest;
import com.inventory.movementservice.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovementService {
    InventoryMovement createMovement(CreateMovementRequest request, String performedBy);
    InventoryMovement getMovementById(Long id);
    InventoryMovement getMovementByNumber(String movementNumber);
    Page<InventoryMovement> getAllMovements(Pageable pageable);
    Page<InventoryMovement> searchMovements(String movementType, String status, Long productId, Long warehouseId, Pageable pageable);
}
