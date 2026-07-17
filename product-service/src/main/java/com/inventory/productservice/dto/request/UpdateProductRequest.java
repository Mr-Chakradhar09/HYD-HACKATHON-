package com.inventory.productservice.dto.request;

import com.inventory.productservice.enums.UnitType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class UpdateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    private String productName;

    private String description;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Unit is required")
    private UnitType unit;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private String imageUrl;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;

    private String barcode;

    private Integer reorderLevel;

    public UpdateProductRequest() {}

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public UnitType getUnit() { return unit; }
    public void setUnit(UnitType unit) { this.unit = unit; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
}
