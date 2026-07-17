package com.inventory.forecastservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    @KafkaListener(topics = "${spring.kafka.topics.movement-events:movement-events}", groupId = "${spring.kafka.consumer.group-id:forecast-service}")
    public void consumeMovementEvent(String eventData) {
        // In a real scenario, this would use a predictive model based on historical movement
        System.out.println("Received movement event for forecasting: " + eventData);
    }
}
