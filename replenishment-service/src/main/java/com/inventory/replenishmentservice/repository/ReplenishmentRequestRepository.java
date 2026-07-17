package com.inventory.replenishmentservice.repository;

import com.inventory.replenishmentservice.entity.ReplenishmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplenishmentRequestRepository extends JpaRepository<ReplenishmentRequest, Long> {
    List<ReplenishmentRequest> findByWarehouseId(Long warehouseId);
}
