package com.inventory.product.dto;

import com.inventory.product.entity.Product;
import com.inventory.product.enums.Category;
import com.inventory.product.enums.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private Category category;
    private BigDecimal unitPrice;
    private String unitOfMeasure;
    private Integer minimumStock;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse fromEntity(Product p) {
        ProductResponse r = new ProductResponse();
        r.id = p.getId(); r.sku = p.getSku(); r.name = p.getName();
        r.description = p.getDescription(); r.category = p.getCategory();
        r.unitPrice = p.getUnitPrice(); r.unitOfMeasure = p.getUnitOfMeasure();
        r.minimumStock = p.getMinimumStock();
        r.status = p.getStatus(); r.createdAt = p.getCreatedAt(); r.updatedAt = p.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public Integer getMinimumStock() { return minimumStock; }
    public ProductStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
