package com.inventory.reportingservice.repository;

import com.inventory.reportingservice.entity.WarehouseReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WarehouseReadModelRepository extends JpaRepository<WarehouseReadModel, Long> {
    Optional<WarehouseReadModel> findByWarehouseId(Long warehouseId);
    boolean existsByWarehouseId(Long warehouseId);
}
