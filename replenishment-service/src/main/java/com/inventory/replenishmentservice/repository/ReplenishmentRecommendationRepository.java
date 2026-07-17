package com.inventory.replenishmentservice.repository;

import com.inventory.replenishmentservice.entity.ReplenishmentRecommendation;
import com.inventory.replenishmentservice.enums.RecommendationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReplenishmentRecommendationRepository extends JpaRepository<ReplenishmentRecommendation, Long> {
    Optional<ReplenishmentRecommendation> findByRecommendationNumber(String number);
    Page<ReplenishmentRecommendation> findByStatus(RecommendationStatus status, Pageable pageable);
    Page<ReplenishmentRecommendation> findByProductIdAndWarehouseId(Long productId, Long warehouseId, Pageable pageable);
}
