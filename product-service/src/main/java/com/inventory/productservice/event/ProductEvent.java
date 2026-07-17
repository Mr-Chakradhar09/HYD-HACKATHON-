package com.inventory.productservice.event;

import java.time.LocalDateTime;

public class ProductEvent {

    private String eventId;
    private String eventType;
    private Long productId;
    private String sku;
    private String productName;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private String performedBy;
    private LocalDateTime timestamp;
    private int eventVersion;

    public ProductEvent() {
        this.timestamp = LocalDateTime.now();
        this.eventVersion = 1;
    }

    public ProductEvent(String eventType, Long productId, String sku, String performedBy) {
        this();
        this.eventId = "PRT_EVENT_" + System.currentTimeMillis();
        this.eventType = eventType;
        this.productId = productId;
        this.sku = sku;
        this.performedBy = performedBy;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }
}
