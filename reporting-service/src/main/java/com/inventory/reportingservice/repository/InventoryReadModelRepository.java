package com.inventory.reportingservice.repository;

import com.inventory.reportingservice.entity.InventoryReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryReadModelRepository extends JpaRepository<InventoryReadModel, Long> {
    Optional<InventoryReadModel> findByInventoryId(Long inventoryId);
    Optional<InventoryReadModel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    boolean existsByInventoryId(Long inventoryId);
    long countByStockStatus(String status);
}
