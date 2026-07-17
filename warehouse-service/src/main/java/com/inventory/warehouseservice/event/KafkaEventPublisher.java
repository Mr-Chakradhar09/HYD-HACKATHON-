package com.inventory.warehouseservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.warehouse-events:warehouse-events}")
    private String warehouseEventsTopic;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishWarehouseEvent(WarehouseEvent event) {
        log.info("Publishing warehouse event: {} for warehouse: {}", event.getEventType(), event.getWarehouseId());
        try {
            kafkaTemplate.send(warehouseEventsTopic, event.getEventId(), event);
        } catch (Exception e) {
            log.warn("Failed to publish warehouse event to Kafka: {}", e.getMessage());
        }
    }

    public void publishAssignmentEvent(WarehouseAssignmentEvent event) {
        log.info("Publishing assignment event: {} for warehouse: {}", event.getEventType(), event.getWarehouseId());
        try {
            kafkaTemplate.send(warehouseEventsTopic, event.getEventId(), event);
        } catch (Exception e) {
            log.warn("Failed to publish assignment event to Kafka: {}", e.getMessage());
        }
    }
}
