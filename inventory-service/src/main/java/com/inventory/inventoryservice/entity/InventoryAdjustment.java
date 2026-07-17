package com.inventory.inventoryservice.entity;

import com.inventory.inventoryservice.enums.AdjustmentType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_adjustments")
public class InventoryAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false)
    private AdjustmentType adjustmentType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String reason;

    private String remarks;

    @Column(name = "adjusted_by", nullable = false)
    private String adjustedBy;

    @Column(name = "adjusted_at", nullable = false)
    private LocalDateTime adjustedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public InventoryAdjustment() {
        this.createdAt = LocalDateTime.now();
        this.adjustedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public AdjustmentType getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(AdjustmentType adjustmentType) { this.adjustmentType = adjustmentType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getAdjustedBy() { return adjustedBy; }
    public void setAdjustedBy(String adjustedBy) { this.adjustedBy = adjustedBy; }
    public LocalDateTime getAdjustedAt() { return adjustedAt; }
    public void setAdjustedAt(LocalDateTime adjustedAt) { this.adjustedAt = adjustedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
