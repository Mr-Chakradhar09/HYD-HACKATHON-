package com.inventory.productservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.product-events:product-events}")
    private String productEventsTopic;

    @Value("${spring.kafka.topics.category-events:category-events}")
    private String categoryEventsTopic;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishProductEvent(ProductEvent event) {
        log.info("Publishing product event: {} for product: {}", event.getEventType(), event.getProductId());
        try {
            kafkaTemplate.send(productEventsTopic, event.getEventId(), event);
        } catch (Exception e) {
            log.warn("Failed to publish product event to Kafka: {}", e.getMessage());
        }
    }

    public void publishCategoryEvent(CategoryEvent event) {
        log.info("Publishing category event: {} for category: {}", event.getEventType(), event.getCategoryId());
        try {
            kafkaTemplate.send(categoryEventsTopic, event.getEventId(), event);
        } catch (Exception e) {
            log.warn("Failed to publish category event to Kafka: {}", e.getMessage());
        }
    }
}
