package com.inventory.inventory.repository;

import com.inventory.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByProductId(Long productId);
    List<InventoryTransaction> findByWarehouseId(Long warehouseId);
    List<InventoryTransaction> findByType(String type);
}
