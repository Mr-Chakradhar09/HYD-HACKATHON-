package com.inventory.movementservice.repository;

import com.inventory.movementservice.entity.InventoryMovement;
import com.inventory.movementservice.enums.MovementStatus;
import com.inventory.movementservice.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    Optional<InventoryMovement> findByMovementNumber(String movementNumber);
    boolean existsByMovementNumber(String movementNumber);
    Page<InventoryMovement> findByMovementType(MovementType movementType, Pageable pageable);
    Page<InventoryMovement> findByMovementStatus(MovementStatus movementStatus, Pageable pageable);
    Page<InventoryMovement> findByProductId(Long productId, Pageable pageable);
    Page<InventoryMovement> findBySourceWarehouseId(Long warehouseId, Pageable pageable);
    Page<InventoryMovement> findByDestinationWarehouseId(Long warehouseId, Pageable pageable);
    Optional<InventoryMovement> findByCorrelationId(String correlationId);
}
