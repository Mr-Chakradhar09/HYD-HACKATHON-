package com.inventory.productservice.event;

import java.time.LocalDateTime;

public class CategoryEvent {

    private String eventId;
    private String eventType;
    private Long categoryId;
    private String categoryName;
    private String performedBy;
    private LocalDateTime timestamp;
    private int eventVersion;

    public CategoryEvent() {
        this.timestamp = LocalDateTime.now();
        this.eventVersion = 1;
    }

    public CategoryEvent(String eventType, Long categoryId, String categoryName, String performedBy) {
        this();
        this.eventId = "CAT_EVENT_" + System.currentTimeMillis();
        this.eventType = eventType;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.performedBy = performedBy;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
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
