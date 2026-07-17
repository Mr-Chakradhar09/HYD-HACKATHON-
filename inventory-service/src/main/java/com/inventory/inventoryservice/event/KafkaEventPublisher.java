package com.inventory.inventoryservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.inventory-events:inventory-events}")
    private String inventoryEventsTopic;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) { this.kafkaTemplate = kafkaTemplate; }

    public void publishInventoryEvent(InventoryEvent event) {
        log.info("Publishing inventory event: {} for inventory: {}", event.getEventType(), event.getInventoryId());
        try {
            kafkaTemplate.send(inventoryEventsTopic, event.getEventId(), event);
        } catch (Exception e) {
            log.warn("Failed to publish inventory event to Kafka: {}", e.getMessage());
        }
    }
}
