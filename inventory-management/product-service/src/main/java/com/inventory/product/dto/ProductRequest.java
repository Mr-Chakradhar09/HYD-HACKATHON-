package com.inventory.product.dto;

import com.inventory.product.enums.Category;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class ProductRequest {
    @NotBlank private String sku;
    @NotBlank private String name;
    private String description;
    private Category category;
    private BigDecimal unitPrice;
    private String unitOfMeasure;
    private Integer minimumStock;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
}
