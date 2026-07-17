package com.inventory.warehouseservice.repository;

import com.inventory.warehouseservice.entity.WarehouseAssignment;
import com.inventory.warehouseservice.enums.WarehouseRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseAssignmentRepository extends JpaRepository<WarehouseAssignment, Long> {

    List<WarehouseAssignment> findByWarehouseIdAndActiveTrue(Long warehouseId);

    List<WarehouseAssignment> findByActiveTrue();

    boolean existsByWarehouseIdAndWarehouseRoleAndActiveTrue(Long warehouseId, WarehouseRole role);

    boolean existsByEmployeeCodeAndActiveTrue(String employeeCode);

    boolean existsByEmployeeIdAndActiveTrue(Long employeeId);

    Optional<WarehouseAssignment> findByIdAndActiveTrue(Long id);

    Page<WarehouseAssignment> findByWarehouseId(Long warehouseId, Pageable pageable);

    Optional<WarehouseAssignment> findByEmployeeCodeAndActiveTrue(String employeeCode);
}
