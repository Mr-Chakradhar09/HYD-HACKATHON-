package com.inventory.reportingservice.repository;

import com.inventory.reportingservice.entity.MovementReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementReadModelRepository extends JpaRepository<MovementReadModel, Long> {
    long countByMovementType(String movementType);
}
