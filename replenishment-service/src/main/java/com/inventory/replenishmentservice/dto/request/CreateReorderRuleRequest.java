package com.inventory.replenishmentservice.dto.request;

import com.inventory.replenishmentservice.enums.ReplenishmentMode;
import jakarta.validation.constraints.*;

public class CreateReorderRuleRequest {

    @NotNull private Long productId;
    @NotNull private Long warehouseId;
    @NotNull @Min(0) private Integer minimumStock;
    @NotNull @Min(1) private Integer maximumStock;
    @NotNull @Min(0) private Integer reorderPoint;
    @NotNull @Min(0) private Integer safetyStock;
    private Integer reorderQuantity;
    @NotNull private ReplenishmentMode replenishmentMode;

    public CreateReorderRuleRequest() {}
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
    public Integer getMaximumStock() { return maximumStock; }
    public void setMaximumStock(Integer maximumStock) { this.maximumStock = maximumStock; }
    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
    public Integer getSafetyStock() { return safetyStock; }
    public void setSafetyStock(Integer safetyStock) { this.safetyStock = safetyStock; }
    public Integer getReorderQuantity() { return reorderQuantity; }
    public void setReorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; }
    public ReplenishmentMode getReplenishmentMode() { return replenishmentMode; }
    public void setReplenishmentMode(ReplenishmentMode replenishmentMode) { this.replenishmentMode = replenishmentMode; }
}
