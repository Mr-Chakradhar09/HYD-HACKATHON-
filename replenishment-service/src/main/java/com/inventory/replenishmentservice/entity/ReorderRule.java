package com.inventory.replenishmentservice.entity;

import com.inventory.replenishmentservice.enums.ReplenishmentMode;
import com.inventory.replenishmentservice.enums.RuleStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reorder_rules")
public class ReorderRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock;

    @Column(name = "maximum_stock", nullable = false)
    private Integer maximumStock;

    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint;

    @Column(name = "safety_stock", nullable = false)
    private Integer safetyStock;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "replenishment_mode", nullable = false)
    private ReplenishmentMode replenishmentMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleStatus status;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public ReorderRule() {
        this.createdAt = LocalDateTime.now();
        this.status = RuleStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public RuleStatus getStatus() { return status; }
    public void setStatus(RuleStatus status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
