package com.inventory.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotent_events")
public class IdempotentEvent {

    @Id
    private String eventId; // E.g. "purchase-request-123"

    private LocalDateTime processedAt;

    public IdempotentEvent() {}

    public IdempotentEvent(String eventId) {
        this.eventId = eventId;
        this.processedAt = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
