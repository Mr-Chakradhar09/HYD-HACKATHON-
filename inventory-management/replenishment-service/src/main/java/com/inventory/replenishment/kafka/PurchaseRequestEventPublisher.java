package com.inventory.replenishment.kafka;

import com.inventory.replenishment.entity.PurchaseRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PurchaseRequestEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public PurchaseRequestEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPurchaseRequestCreated(PurchaseRequest pr) {
        String message = String.format(
            "{\"id\":%d,\"productId\":%d,\"warehouseId\":%d,\"requiredQuantity\":%d,\"status\":\"%s\"}",
            pr.getId(), pr.getProductId(), pr.getWarehouseId(), pr.getRequiredQuantity(), pr.getStatus()
        );
        kafkaTemplate.send("purchase-request-created", message);
    }
}
