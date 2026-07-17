package com.inventory.movementservice.event;

import com.inventory.movementservice.entity.InventoryMovement;
import com.inventory.movementservice.enums.MovementStatus;
import com.inventory.movementservice.enums.MovementType;
import com.inventory.movementservice.enums.ReferenceType;
import com.inventory.movementservice.repository.InventoryMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InventoryEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventKafkaConsumer.class);
    private final InventoryMovementRepository movementRepository;

    private static final AtomicLong counter = new AtomicLong(1);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public InventoryEventKafkaConsumer(InventoryMovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @KafkaListener(topics = "${spring.kafka.topics.inventory-events:inventory-events}", groupId = "${spring.kafka.consumer.group-id:movement-service}")
    @Transactional
    public void consumeInventoryEvent(InventoryEvent event) {
        log.info("Received inventory event: {} for product: {} warehouse: {}",
                event.getEventType(), event.getProductId(), event.getWarehouseId());

        MovementType movementType = mapEventTypeToMovementType(event.getEventType());
        if (movementType == null) {
            log.debug("Skipping inventory event: {} (no movement mapping)", event.getEventType());
            return;
        }

        if (movementRepository.existsByMovementNumber(event.getMovementNumber())) {
            log.debug("Movement {} already exists, skipping", event.getMovementNumber());
            return;
        }

        InventoryMovement movement = new InventoryMovement();
        movement.setMovementNumber(event.getMovementNumber() != null ? event.getMovementNumber() : generateMovementNumber());
        movement.setMovementType(movementType);
        movement.setProductId(event.getProductId());
        movement.setQuantity(Math.abs(event.getChangeQuantity() != null ? event.getChangeQuantity() : 0));
        movement.setMovementStatus(MovementStatus.COMPLETED);
        movement.setReferenceType(ReferenceType.ADJUSTMENT_REQUEST);
        movement.setReferenceNumber(event.getMovementNumber());
        movement.setPerformedBy(event.getPerformedBy() != null ? event.getPerformedBy() : "SYSTEM");
        movement.setCorrelationId(event.getEventId());
        movement.setRemarks("Auto-recorded from inventory-event: " + event.getEventType());

        if (movementType == MovementType.ADJUSTMENT_INCREASE || movementType == MovementType.INITIAL_STOCK) {
            movement.setDestinationWarehouseId(event.getWarehouseId());
        } else if (movementType == MovementType.ADJUSTMENT_DECREASE || movementType == MovementType.DAMAGE) {
            movement.setSourceWarehouseId(event.getWarehouseId());
        } else if (movementType == MovementType.TRANSFER_OUT) {
            movement.setSourceWarehouseId(event.getWarehouseId());
        } else if (movementType == MovementType.TRANSFER_IN) {
            movement.setDestinationWarehouseId(event.getWarehouseId());
        }

        movementRepository.save(movement);
        log.info("Auto-created movement: {} for inventory event: {}", movement.getMovementNumber(), event.getEventType());
    }

    private MovementType mapEventTypeToMovementType(String eventType) {
        if (eventType == null) return null;
        return switch (eventType) {
            case "StockAdjustedIncrease", "StockInitialStock" -> MovementType.ADJUSTMENT_INCREASE;
            case "StockAdjustedDecrease", "StockCycleCount" -> MovementType.ADJUSTMENT_DECREASE;
            case "StockAdjustedCorrection" -> MovementType.ADJUSTMENT_INCREASE;
            case "StockDamaged" -> MovementType.DAMAGE;
            case "StockReserved" -> MovementType.GOODS_ISSUE;
            case "StockReleased" -> MovementType.GOODS_RECEIPT;
            case "StockTransferredOut" -> MovementType.TRANSFER_OUT;
            case "StockTransferredIn" -> MovementType.TRANSFER_IN;
            default -> null;
        };
    }

    private String generateMovementNumber() {
        String dateStr = LocalDate.now().format(formatter);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("MOV-%s-%s", dateStr, uniqueId);
    }
}
