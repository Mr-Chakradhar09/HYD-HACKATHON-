package com.inventory.inventory.kafka;

import com.inventory.inventory.entity.InventoryTransaction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInventoryUpdated(InventoryTransaction transaction) {
        String message = String.format(
            "{\"type\":\"%s\",\"productId\":%d,\"warehouseId\":%d,\"quantity\":%d,\"reference\":\"%s\"}",
            transaction.getType(), transaction.getProductId(), transaction.getWarehouseId(),
            transaction.getQuantity(), transaction.getReference() != null ? transaction.getReference() : ""
        );
        kafkaTemplate.send("inventory-updated", message);
    }

    public void publishStockTransferred(Long productId, Long fromWarehouse, Long toWarehouse, Integer quantity) {
        String message = String.format(
            "{\"productId\":%d,\"fromWarehouse\":%d,\"toWarehouse\":%d,\"quantity\":%d}",
            productId, fromWarehouse, toWarehouse, quantity
        );
        kafkaTemplate.send("stock-transferred", message);
    }

    public void publishInventoryReplenished(Long purchaseRequestId) {
        String message = String.format("{\"purchaseRequestId\":%d,\"status\":\"SUCCESS\"}", purchaseRequestId);
        kafkaTemplate.send("inventory-replenished", message);
    }

    public void publishInventoryReplenishmentFailed(Long purchaseRequestId, String reason) {
        String message = String.format("{\"purchaseRequestId\":%d,\"status\":\"FAILED\",\"reason\":\"%s\"}", purchaseRequestId, reason);
        kafkaTemplate.send("inventory-replenishment-failed", message);
    }
}
