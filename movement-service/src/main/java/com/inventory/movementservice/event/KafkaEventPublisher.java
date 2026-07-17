package com.inventory.movementservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.movement-events:movement-events}")
    private String movementEventsTopic;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) { this.kafkaTemplate = kafkaTemplate; }

    public void publishMovementEvent(MovementEvent event) {
        log.info("Publishing movement event: {} for movement: {}", event.getEventType(), event.getMovementNumber());
        try {
            kafkaTemplate.send(movementEventsTopic, event.getEventId(), event);
        } catch (Exception e) {
            log.warn("Failed to publish movement event to Kafka: {}", e.getMessage());
        }
    }
}
