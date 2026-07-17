package com.inventory.reportingservice.repository;

import com.inventory.reportingservice.entity.MovementHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementHistoryRepository extends JpaRepository<MovementHistory, Long> {
    Page<MovementHistory> findByProductId(Long productId, Pageable pageable);
    Page<MovementHistory> findBySourceWarehouseIdOrDestinationWarehouseId(Long sourceId, Long destId, Pageable pageable);
}
