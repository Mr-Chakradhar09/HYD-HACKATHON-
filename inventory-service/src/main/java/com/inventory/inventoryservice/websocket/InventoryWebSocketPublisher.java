package com.inventory.inventoryservice.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public InventoryWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendInventoryUpdate(InventoryNotification notification) {
        messagingTemplate.convertAndSend("/topic/inventory", notification);
    }
}
