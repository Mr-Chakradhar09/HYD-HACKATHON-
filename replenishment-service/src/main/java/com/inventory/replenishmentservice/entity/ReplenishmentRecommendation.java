package com.inventory.replenishmentservice.entity;

import com.inventory.replenishmentservice.enums.RecommendationStatus;
import com.inventory.replenishmentservice.enums.ReplenishmentType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "replenishment_recommendations")
public class ReplenishmentRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recommendation_number", nullable = false, unique = true)
    private String recommendationNumber;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "replenishment_type", nullable = false)
    private ReplenishmentType replenishmentType;

    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(name = "reserved_stock")
    private Integer reservedStock;

    @Column(name = "available_stock")
    private Integer availableStock;

    @Column(name = "recommended_quantity", nullable = false)
    private Integer recommendedQuantity;

    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationStatus status;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String approvedBy;

    private LocalDateTime approvedAt;

    private String rejectionReason;

    @Version
    private Long version;

    public ReplenishmentRecommendation() {
        this.createdAt = LocalDateTime.now();
        this.status = RecommendationStatus.PENDING_APPROVAL;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecommendationNumber() { return recommendationNumber; }
    public void setRecommendationNumber(String recommendationNumber) { this.recommendationNumber = recommendationNumber; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public ReplenishmentType getReplenishmentType() { return replenishmentType; }
    public void setReplenishmentType(ReplenishmentType replenishmentType) { this.replenishmentType = replenishmentType; }
    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
    public Integer getReservedStock() { return reservedStock; }
    public void setReservedStock(Integer reservedStock) { this.reservedStock = reservedStock; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public Integer getRecommendedQuantity() { return recommendedQuantity; }
    public void setRecommendedQuantity(Integer recommendedQuantity) { this.recommendedQuantity = recommendedQuantity; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public RecommendationStatus getStatus() { return status; }
    public void setStatus(RecommendationStatus status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Long getVersion() { return version; }
}
