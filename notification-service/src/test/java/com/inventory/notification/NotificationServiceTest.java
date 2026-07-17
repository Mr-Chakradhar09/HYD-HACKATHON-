package com.inventory.notification;

import com.inventory.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    void processInventoryUpdated_ShouldNotThrow() {
        String message = "{\"type\":\"INBOUND\",\"productId\":1,\"warehouseId\":1,\"quantity\":100,\"reference\":\"PO-001\"}";
        assertDoesNotThrow(() -> notificationService.processInventoryUpdated(message));
    }

    @Test
    void processInventoryUpdated_OutboundLowStock_ShouldNotThrow() {
        String message = "{\"type\":\"OUTBOUND\",\"productId\":1,\"warehouseId\":1,\"quantity\":5,\"reference\":\"SO-001\"}";
        assertDoesNotThrow(() -> notificationService.processInventoryUpdated(message));
    }

    @Test
    void processInventoryUpdated_InvalidJson_ShouldNotThrow() {
        assertDoesNotThrow(() -> notificationService.processInventoryUpdated("invalid json"));
    }

    @Test
    void processStockTransferred_ShouldNotThrow() {
        String message = "{\"productId\":1,\"fromWarehouse\":1,\"toWarehouse\":2,\"quantity\":30}";
        assertDoesNotThrow(() -> notificationService.processStockTransferred(message));
    }

    @Test
    void processPurchaseRequestCreated_ShouldNotThrow() {
        String message = "{\"id\":1,\"productId\":1,\"warehouseId\":1,\"requiredQuantity\":50,\"status\":\"PENDING\"}";
        assertDoesNotThrow(() -> notificationService.processPurchaseRequestCreated(message));
    }
}
