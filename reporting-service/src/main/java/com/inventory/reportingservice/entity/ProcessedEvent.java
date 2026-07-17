package com.inventory.reportingservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false) private String eventId;
    @Column(nullable = false) private String eventType;
    @Column(nullable = false) private LocalDateTime processedAt;

    public ProcessedEvent() { this.processedAt = LocalDateTime.now(); }
    public ProcessedEvent(String eventId, String eventType) { this(); this.eventId = eventId; this.eventType = eventType; }
    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
