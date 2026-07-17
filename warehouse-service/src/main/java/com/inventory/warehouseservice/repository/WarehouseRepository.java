package com.inventory.warehouseservice.repository;

import com.inventory.warehouseservice.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, JpaSpecificationExecutor<Warehouse> {

    boolean existsByWarehouseCode(String warehouseCode);

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);
}