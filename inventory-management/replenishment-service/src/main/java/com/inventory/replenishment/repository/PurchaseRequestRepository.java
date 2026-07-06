package com.inventory.replenishment.repository;

import com.inventory.replenishment.entity.PurchaseRequest;
import com.inventory.replenishment.enums.PurchaseRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    List<PurchaseRequest> findByStatus(PurchaseRequestStatus status);
    List<PurchaseRequest> findByProductId(Long productId);
    List<PurchaseRequest> findByWarehouseId(Long warehouseId);
}
