package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.request.CreateAdjustmentRequest;
import com.inventory.inventoryservice.entity.Inventory;
import com.inventory.inventoryservice.entity.InventoryAdjustment;
import com.inventory.inventoryservice.enums.AdjustmentType;
import com.inventory.inventoryservice.event.InventoryEvent;
import com.inventory.inventoryservice.event.KafkaEventPublisher;
import com.inventory.inventoryservice.exception.InsufficientStockException;
import com.inventory.inventoryservice.exception.InvalidOperationException;
import com.inventory.inventoryservice.exception.ResourceNotFoundException;
import com.inventory.inventoryservice.repository.InventoryAdjustmentRepository;
import com.inventory.inventoryservice.repository.InventoryRepository;
import com.inventory.inventoryservice.service.AdjustmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdjustmentServiceImpl implements AdjustmentService {

    private final InventoryRepository inventoryRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;
    private final KafkaEventPublisher eventPublisher;

    public AdjustmentServiceImpl(InventoryRepository inventoryRepository, InventoryAdjustmentRepository adjustmentRepository, KafkaEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public InventoryAdjustment createAdjustment(CreateAdjustmentRequest request, String adjustedBy) {
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        int previousQty = inventory.getCurrentQuantity();
        boolean isDecrease = request.getAdjustmentType() == AdjustmentType.DECREASE ||
                             request.getAdjustmentType() == AdjustmentType.DAMAGE ||
                             request.getAdjustmentType() == AdjustmentType.CORRECTION;

        if (isDecrease && inventory.getCurrentQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for adjustment");
        }

        if (isDecrease) {
            inventory.setCurrentQuantity(inventory.getCurrentQuantity() - request.getQuantity());
            if (request.getAdjustmentType() == AdjustmentType.DAMAGE) {
                inventory.setDamagedQuantity(inventory.getDamagedQuantity() + request.getQuantity());
            }
        } else {
            inventory.setCurrentQuantity(inventory.getCurrentQuantity() + request.getQuantity());
        }
        inventory.recalculateAvailable();
        inventoryRepository.save(inventory);

        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setInventory(inventory);
        adjustment.setAdjustmentType(request.getAdjustmentType());
        adjustment.setQuantity(request.getQuantity());
        adjustment.setReason(request.getReason());
        adjustment.setRemarks(request.getRemarks());
        adjustment.setAdjustedBy(adjustedBy);
        adjustment = adjustmentRepository.save(adjustment);

        String eventType = mapAdjustmentTypeToEvent(request.getAdjustmentType());
        InventoryEvent event = new InventoryEvent(eventType, inventory.getId(), inventory.getProductId(), inventory.getWarehouseId());
        event.setPreviousQuantity(previousQty);
        event.setNewQuantity(inventory.getCurrentQuantity());
        event.setChangeQuantity(isDecrease ? -request.getQuantity() : request.getQuantity());
        event.setPerformedBy(adjustedBy);
        event.setMovementNumber("ADJ-" + adjustment.getId());
        eventPublisher.publishInventoryEvent(event);

        return adjustment;
    }

    private String mapAdjustmentTypeToEvent(AdjustmentType type) {
        return switch (type) {
            case INCREASE -> "StockAdjustedIncrease";
            case DECREASE -> "StockAdjustedDecrease";
            case DAMAGE -> "StockDamaged";
            case CORRECTION -> "StockAdjustedCorrection";
            case INITIAL_STOCK -> "StockInitialStock";
            case CYCLE_COUNT -> "StockCycleCount";
        };
    }
}
