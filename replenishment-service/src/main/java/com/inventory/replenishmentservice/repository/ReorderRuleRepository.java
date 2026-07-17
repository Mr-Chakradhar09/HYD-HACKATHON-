package com.inventory.replenishmentservice.repository;

import com.inventory.replenishmentservice.entity.ReorderRule;
import com.inventory.replenishmentservice.enums.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReorderRuleRepository extends JpaRepository<ReorderRule, Long> {
    Optional<ReorderRule> findByProductIdAndWarehouseIdAndStatus(Long productId, Long warehouseId, RuleStatus status);
    boolean existsByProductIdAndWarehouseIdAndStatus(Long productId, Long warehouseId, RuleStatus status);
}
