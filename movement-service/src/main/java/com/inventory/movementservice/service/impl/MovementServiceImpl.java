package com.inventory.movementservice.service.impl;

import com.inventory.movementservice.dto.request.CreateMovementRequest;
import com.inventory.movementservice.entity.InventoryMovement;
import com.inventory.movementservice.enums.MovementStatus;
import com.inventory.movementservice.enums.MovementType;
import com.inventory.movementservice.event.KafkaEventPublisher;
import com.inventory.movementservice.event.MovementEvent;
import com.inventory.movementservice.exception.InvalidMovementException;
import com.inventory.movementservice.exception.ResourceNotFoundException;
import com.inventory.movementservice.feign.ProductFeignClient;
import com.inventory.movementservice.feign.WarehouseFeignClient;
import com.inventory.movementservice.repository.InventoryMovementRepository;
import com.inventory.movementservice.service.MovementService;
import com.inventory.movementservice.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MovementServiceImpl implements MovementService {

    private static final Logger log = LoggerFactory.getLogger(MovementServiceImpl.class);
    private final InventoryMovementRepository movementRepository;
    private final ProductFeignClient productFeignClient;
    private final WarehouseFeignClient warehouseFeignClient;
    private final KafkaEventPublisher eventPublisher;

    private static final AtomicLong counter = new AtomicLong(1);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public MovementServiceImpl(InventoryMovementRepository movementRepository,
                               ProductFeignClient productFeignClient,
                               WarehouseFeignClient warehouseFeignClient,
                               KafkaEventPublisher eventPublisher) {
        this.movementRepository = movementRepository;
        this.productFeignClient = productFeignClient;
        this.warehouseFeignClient = warehouseFeignClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public InventoryMovement createMovement(CreateMovementRequest request, String performedBy) {
        validateMovement(request);

        // Transfers must go through the approval workflow (TransferRequestController)
        MovementType type = request.getMovementType();
        if (type == MovementType.TRANSFER_OUT || type == MovementType.TRANSFER_IN) {
            throw new InvalidMovementException(
                    "Inter-warehouse transfers must be created through the transfer request workflow. "
                    + "Use POST /api/v1/transfer-requests to create a transfer request for approval.");
        }

        // Best-effort validation using Feign Clients
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String authHeader = attrs != null ? attrs.getRequest().getHeader("Authorization") : null;

        try {
            productFeignClient.getProductById(request.getProductId(), authHeader);
        } catch (Exception e) {
            log.warn("Could not validate product id {} via Feign, proceeding anyway: {}", request.getProductId(), e.getMessage());
        }

        if (request.getSourceWarehouseId() != null) {
            try {
                warehouseFeignClient.getWarehouseById(request.getSourceWarehouseId());
            } catch (Exception e) {
                log.warn("Could not validate source warehouse id {} via Feign, proceeding anyway: {}", request.getSourceWarehouseId(), e.getMessage());
            }
        }

        if (request.getDestinationWarehouseId() != null) {
            try {
                warehouseFeignClient.getWarehouseById(request.getDestinationWarehouseId());
            } catch (Exception e) {
                log.warn("Could not validate destination warehouse id {} via Feign, proceeding anyway: {}", request.getDestinationWarehouseId(), e.getMessage());
            }
        }

        InventoryMovement movement = new InventoryMovement();
        movement.setMovementNumber(generateMovementNumber());
        movement.setMovementType(request.getMovementType());
        movement.setProductId(request.getProductId());
        movement.setSourceWarehouseId(request.getSourceWarehouseId());
        movement.setDestinationWarehouseId(request.getDestinationWarehouseId());
        movement.setQuantity(request.getQuantity());
        movement.setReferenceType(request.getReferenceType());
        movement.setReferenceNumber(request.getReferenceNumber());
        movement.setRemarks(request.getRemarks());
        movement.setBatchNumber(request.getBatchNumber());
        movement.setCorrelationId(request.getCorrelationId());
        movement.setPerformedBy(performedBy);
        movement.setMovementStatus(MovementStatus.COMPLETED); // Setting immediately to COMPLETED per instruction

        movement = movementRepository.save(movement);

        // Publish event
        String mappedEventType = mapEventType(movement.getMovementType());
        MovementEvent event = new MovementEvent(mappedEventType, String.valueOf(movement.getId()), movement.getMovementNumber());
        event.setMovementType(movement.getMovementType().name());
        event.setProductId(movement.getProductId());
        event.setSourceWarehouseId(movement.getSourceWarehouseId());
        event.setDestinationWarehouseId(movement.getDestinationWarehouseId());
        event.setQuantity(movement.getQuantity());
        event.setPerformedBy(movement.getPerformedBy());
        
        eventPublisher.publishMovementEvent(event);

        return movement;
    }

    private String mapEventType(MovementType type) {
        switch (type) {
            case GOODS_RECEIPT: return "StockReceived";
            case GOODS_ISSUE: return "StockIssued";
            case TRANSFER_OUT: return "StockTransferredOut";
            case TRANSFER_IN: return "StockTransferredIn";
            case RETURN_IN:
            case RETURN_OUT: return "StockReturned";
            case ADJUSTMENT_INCREASE:
            case ADJUSTMENT_DECREASE:
            case CYCLE_COUNT: return "StockAdjusted";
            case DAMAGE:
            case DISPOSAL: return "StockDamaged";
            case INITIAL_STOCK: return "StockReceived";
            default: return "StockAdjusted";
        }
    }

    @Override
    public InventoryMovement getMovementById(Long id) {
        return movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found with id: " + id));
    }

    @Override
    public InventoryMovement getMovementByNumber(String movementNumber) {
        return movementRepository.findByMovementNumber(movementNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found: " + movementNumber));
    }

    @Override
    public Page<InventoryMovement> getAllMovements(Pageable pageable) {
        return movementRepository.findAll(pageable);
    }

    @Override
    public Page<InventoryMovement> searchMovements(String movementType, String status, Long productId, Long warehouseId, Pageable pageable) {
        if (movementType != null) {
            return movementRepository.findByMovementType(MovementType.valueOf(movementType), pageable);
        }
        if (status != null) {
            return movementRepository.findByMovementStatus(MovementStatus.valueOf(status), pageable);
        }
        if (productId != null) {
            return movementRepository.findByProductId(productId, pageable);
        }
        if (warehouseId != null) {
            return movementRepository.findBySourceWarehouseId(warehouseId, pageable);
        }
        return movementRepository.findAll(pageable);
    }

    private void validateMovement(CreateMovementRequest request) {
        MovementType type = request.getMovementType();
        if (type == MovementType.GOODS_RECEIPT || type == MovementType.TRANSFER_IN ||
            type == MovementType.RETURN_IN || type == MovementType.ADJUSTMENT_INCREASE ||
            type == MovementType.INITIAL_STOCK) {
            if (request.getDestinationWarehouseId() == null) {
                throw new InvalidMovementException("Destination warehouse is required for " + type);
            }
        }
        if (type == MovementType.GOODS_ISSUE || type == MovementType.TRANSFER_OUT ||
            type == MovementType.RETURN_OUT || type == MovementType.ADJUSTMENT_DECREASE ||
            type == MovementType.DAMAGE || type == MovementType.DISPOSAL) {
            if (request.getSourceWarehouseId() == null) {
                throw new InvalidMovementException("Source warehouse is required for " + type);
            }
        }
        if (type == MovementType.TRANSFER_OUT || type == MovementType.TRANSFER_IN) {
            if (request.getSourceWarehouseId() == null || request.getDestinationWarehouseId() == null) {
                throw new InvalidMovementException("Both source and destination warehouses required for transfer");
            }
            if (request.getSourceWarehouseId().equals(request.getDestinationWarehouseId())) {
                throw new InvalidMovementException("Source and destination warehouses must be different");
            }
        }
    }

    private String generateMovementNumber() {
        String dateStr = LocalDate.now().format(formatter);
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("MOV-%s-%s", dateStr, uniqueId);
    }
}
