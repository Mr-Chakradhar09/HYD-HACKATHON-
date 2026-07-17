package com.inventory.productservice.repository;

import com.inventory.productservice.entity.Product;
import com.inventory.productservice.enums.ProductStatus;
import com.inventory.productservice.enums.UnitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    boolean existsBySku(String sku);
    boolean existsByBarcode(String barcode);
    boolean existsBySkuAndIdNot(String sku, Long id);
    boolean existsByBarcodeAndIdNot(String barcode, Long id);
    boolean existsByCategoryIdAndStatus(Long categoryId, ProductStatus status);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(p.category.name) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(p.barcode) LIKE LOWER(CONCAT('%',:search,'%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:brand IS NULL OR :brand = '' OR LOWER(p.brand) = LOWER(:brand)) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:unit IS NULL OR p.unit = :unit) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProducts(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("status") ProductStatus status,
            @Param("unit") UnitType unit,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable);
}
