package com.inventory.replenishmentservice.dto.request;

import jakarta.validation.constraints.*;

public class CreateManualRecommendationRequest {

    @NotNull private Long productId;
    @NotNull private Long warehouseId;
    @NotNull @Min(1) private Integer recommendedQuantity;
    private Integer priority;

    public CreateManualRecommendationRequest() {}
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Integer getRecommendedQuantity() { return recommendedQuantity; }
    public void setRecommendedQuantity(Integer recommendedQuantity) { this.recommendedQuantity = recommendedQuantity; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
