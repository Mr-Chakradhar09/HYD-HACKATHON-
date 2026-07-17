package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.Inventory;
import com.inventory.inventoryservice.enums.InventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);
    Page<Inventory> findByStatus(InventoryStatus status, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "CAST(i.productId AS string) LIKE CONCAT('%',:search,'%') OR " +
           "CAST(i.warehouseId AS string) LIKE CONCAT('%',:search,'%')) AND " +
           "(:warehouseId IS NULL OR i.warehouseId = :warehouseId) AND " +
           "(:productId IS NULL OR i.productId = :productId) AND " +
           "(:status IS NULL OR i.status = :status)")
    Page<Inventory> searchInventories(
            @Param("search") String search,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("status") InventoryStatus status,
            Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.status = 'ACTIVE' AND i.availableQuantity <= :threshold")
    Page<Inventory> findLowStock(@Param("threshold") Integer threshold, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.status = 'ACTIVE' AND i.availableQuantity = 0")
    Page<Inventory> findOutOfStock(Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.status = 'ACTIVE'")
    java.util.List<Inventory> findByProductIdAndStatusActive(@Param("productId") Long productId);
}
