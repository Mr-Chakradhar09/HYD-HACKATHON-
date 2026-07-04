package com.inventory.notification.consumer;

import com.inventory.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private final NotificationService notificationService;

    public EventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "inventory-updated", groupId = "notification-group")
    public void consumeInventoryUpdated(String message) {
        notificationService.processInventoryUpdated(message);
    }

    @KafkaListener(topics = "stock-transferred", groupId = "notification-group")
    public void consumeStockTransferred(String message) {
        notificationService.processStockTransferred(message);
    }

    @KafkaListener(topics = "purchase-request-created", groupId = "notification-group")
    public void consumePurchaseRequestCreated(String message) {
        notificationService.processPurchaseRequestCreated(message);
    }
}
