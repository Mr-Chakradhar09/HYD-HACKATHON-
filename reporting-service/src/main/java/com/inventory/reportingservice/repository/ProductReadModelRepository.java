package com.inventory.reportingservice.repository;

import com.inventory.reportingservice.entity.ProductReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductReadModelRepository extends JpaRepository<ProductReadModel, Long> {
    Optional<ProductReadModel> findByProductId(Long productId);
    boolean existsByProductId(Long productId);
}
