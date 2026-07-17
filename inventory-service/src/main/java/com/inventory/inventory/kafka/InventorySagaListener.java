package com.inventory.inventory.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.inventory.dto.InventoryRequest;
import com.inventory.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.inventory.inventory.entity.IdempotentEvent;
import com.inventory.inventory.repository.IdempotentEventRepository;

@Component
public class InventorySagaListener {

    private static final Logger log = LoggerFactory.getLogger(InventorySagaListener.class);
    private final InventoryService inventoryService;
    private final InventoryEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final IdempotentEventRepository idempotentEventRepository;

    public InventorySagaListener(InventoryService inventoryService, InventoryEventPublisher eventPublisher, ObjectMapper objectMapper, IdempotentEventRepository idempotentEventRepository) {
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.idempotentEventRepository = idempotentEventRepository;
    }

    @KafkaListener(topics = "purchase-request-created", groupId = "inventory-saga-group")
    public void handlePurchaseRequestCreated(String message) {
        log.info("Received purchase-request-created event: {}", message);
        Long purchaseRequestId = null;
        try {
            JsonNode root = objectMapper.readTree(message);
            purchaseRequestId = root.get("id").asLong();
            Long productId = root.get("productId").asLong();
            Long warehouseId = root.get("warehouseId").asLong();
            Integer requiredQuantity = root.get("requiredQuantity").asInt();

            String eventId = "purchase-request-" + purchaseRequestId;
            if (idempotentEventRepository.existsById(eventId)) {
                log.info("Event {} already processed. Ignoring.", eventId);
                return;
            }

            if (requiredQuantity <= 0) {
                throw new IllegalArgumentException("Required quantity must be greater than 0");
            }

            // Perform the local transaction (Add to inventory)
            InventoryRequest request = new InventoryRequest();
            request.setProductId(productId);
            request.setWarehouseId(warehouseId);
            request.setQuantity(requiredQuantity);
            request.setReference("REPLENISHMENT:" + purchaseRequestId);
            
            inventoryService.inbound(request);
            idempotentEventRepository.save(new IdempotentEvent(eventId));
            
            log.info("Successfully added stock for purchase request: {}", purchaseRequestId);
            eventPublisher.publishInventoryReplenished(purchaseRequestId);

        } catch (Exception e) {
            log.error("Failed to process purchase request {}: {}", purchaseRequestId, e.getMessage());
            if (purchaseRequestId != null) {
                eventPublisher.publishInventoryReplenishmentFailed(purchaseRequestId, e.getMessage());
            }
        }
    }
}
