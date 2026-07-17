package com.inventory.warehouseservice.config;

import com.inventory.warehouseservice.entity.Warehouse;
import com.inventory.warehouseservice.enums.WarehouseStatus;
import org.springframework.data.jpa.domain.Specification;

public class WarehouseSpecification {
    private WarehouseSpecification() {
    }
    public static Specification<Warehouse> hasStatus(WarehouseStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
    public static Specification<Warehouse> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("warehouseCode")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("warehouseName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address").get("city")), pattern)
            );
        };
    }
}