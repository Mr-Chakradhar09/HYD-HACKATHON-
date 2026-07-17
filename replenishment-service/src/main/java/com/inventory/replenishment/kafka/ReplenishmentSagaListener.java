package com.inventory.replenishment.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.replenishment.entity.PurchaseRequest;
import com.inventory.replenishment.enums.PurchaseRequestStatus;
import com.inventory.replenishment.repository.PurchaseRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReplenishmentSagaListener {

    private static final Logger log = LoggerFactory.getLogger(ReplenishmentSagaListener.class);
    private final PurchaseRequestRepository repository;
    private final ObjectMapper objectMapper;

    public ReplenishmentSagaListener(PurchaseRequestRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "inventory-replenished", groupId = "replenishment-saga-group")
    @Transactional
    public void handleInventoryReplenished(String message) {
        log.info("Received inventory-replenished event: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            Long purchaseRequestId = root.get("purchaseRequestId").asLong();

            repository.findById(purchaseRequestId).ifPresent(pr -> {
                pr.setStatus(PurchaseRequestStatus.APPROVED);
                repository.save(pr);
                log.info("Successfully updated PurchaseRequest {} to APPROVED", purchaseRequestId);
            });
        } catch (Exception e) {
            log.error("Error processing inventory-replenished event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "inventory-replenishment-failed", groupId = "replenishment-saga-group")
    @Transactional
    public void handleInventoryReplenishmentFailed(String message) {
        log.info("Received inventory-replenishment-failed event: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            Long purchaseRequestId = root.get("purchaseRequestId").asLong();
            String reason = root.has("reason") ? root.get("reason").asText() : "Unknown error";

            repository.findById(purchaseRequestId).ifPresent(pr -> {
                pr.setStatus(PurchaseRequestStatus.REJECTED);
                // Ideally we would save the reason somewhere, but sticking to existing entity structure
                repository.save(pr);
                log.info("Compensated PurchaseRequest {}. Status set to REJECTED. Reason: {}", purchaseRequestId, reason);
            });
        } catch (Exception e) {
            log.error("Error processing inventory-replenishment-failed event: {}", e.getMessage());
        }
    }
}
