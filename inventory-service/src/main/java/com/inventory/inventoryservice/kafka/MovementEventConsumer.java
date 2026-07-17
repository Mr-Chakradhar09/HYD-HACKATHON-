package com.inventory.inventoryservice.kafka;

import com.inventory.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MovementEventConsumer {

    private final InventoryService inventoryService;

    public MovementEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "${spring.kafka.topics.movement-events:movement-events}", groupId = "${spring.kafka.consumer.group-id:inventory-service}")
    public void consumeMovementEvent(MovementEvent event) {
        inventoryService.updateInventoryFromMovementEvent(event);
    }
}
