package com.inventory.notification.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationSagaListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationSagaListener.class);

    @KafkaListener(topics = "inventory-replenished", groupId = "notification-saga-group")
    public void handleInventoryReplenished(String message) {
        log.info("[NOTIFICATION] SUCCESS: Inventory replenished successfully. Details: {}", message);
        // Here we could send an email to the warehouse manager
    }

    @KafkaListener(topics = "inventory-replenishment-failed", groupId = "notification-saga-group")
    public void handleInventoryReplenishmentFailed(String message) {
        log.warn("[NOTIFICATION] ALERT: Inventory replenishment failed! Details: {}", message);
        // Here we could send an urgent SMS to the purchasing department
    }
}
