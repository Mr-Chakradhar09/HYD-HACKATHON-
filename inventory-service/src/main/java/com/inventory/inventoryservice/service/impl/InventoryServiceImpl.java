package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.request.CreateInventoryRequest;
import com.inventory.inventoryservice.dto.response.InventoryResponse;
import com.inventory.inventoryservice.dto.response.TransferRecommendationResponse;
import com.inventory.inventoryservice.entity.Inventory;
import com.inventory.inventoryservice.enums.InventoryStatus;
import com.inventory.inventoryservice.exception.DuplicateResourceException;
import com.inventory.inventoryservice.exception.IncompatibleOptimisticLockException;
import com.inventory.inventoryservice.exception.InvalidOperationException;
import com.inventory.inventoryservice.exception.ResourceNotFoundException;
import com.inventory.inventoryservice.repository.InventoryRepository;
import com.inventory.inventoryservice.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final com.inventory.inventoryservice.websocket.InventoryWebSocketPublisher webSocketPublisher;
    private final com.inventory.inventoryservice.feign.ProductFeignClient productFeignClient;
    private final com.inventory.inventoryservice.feign.WarehouseFeignClient warehouseFeignClient;
    private final com.inventory.inventoryservice.repository.ProcessedEventRepository processedEventRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, com.inventory.inventoryservice.websocket.InventoryWebSocketPublisher webSocketPublisher, com.inventory.inventoryservice.feign.ProductFeignClient productFeignClient, com.inventory.inventoryservice.feign.WarehouseFeignClient warehouseFeignClient, com.inventory.inventoryservice.repository.ProcessedEventRepository processedEventRepository) {
        this.inventoryRepository = inventoryRepository;
        this.webSocketPublisher = webSocketPublisher;
        this.productFeignClient = productFeignClient;
        this.warehouseFeignClient = warehouseFeignClient;
        this.processedEventRepository = processedEventRepository;
    }

    @Override
    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request, String createdBy) {
        if (inventoryRepository.existsByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())) {
            throw new DuplicateResourceException("Inventory already exists for this product and warehouse combination");
        }
        int qty = request.getInitialQuantity() != null ? request.getInitialQuantity() : 0;
        Inventory inventory = new Inventory();
        inventory.setProductId(request.getProductId());
        inventory.setWarehouseId(request.getWarehouseId());
        inventory.setCurrentQuantity(qty);
        inventory.setReservedQuantity(0);
        inventory.setAvailableQuantity(qty);
        inventory.setDamagedQuantity(0);
        inventory.setStatus(InventoryStatus.ACTIVE);
        inventory.setCreatedBy(createdBy);
        inventoryRepository.save(inventory);
        return mapToResponse(inventory);
    }

    @Override
    public InventoryResponse getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        return mapToResponse(inventory);
    }

    @Override
    public Page<InventoryResponse> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<InventoryResponse> searchInventories(String search, Long warehouseId, Long productId, InventoryStatus status, Pageable pageable) {
        return inventoryRepository.searchInventories(search, warehouseId, productId, status, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deactivateInventory(Long id, String updatedBy) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        if (inventory.getCurrentQuantity() > 0 || inventory.getReservedQuantity() > 0 || inventory.getDamagedQuantity() > 0) {
            throw new InvalidOperationException("Cannot deactivate inventory with existing stock");
        }
        inventory.setStatus(InventoryStatus.INACTIVE);
        inventory.setUpdatedBy(updatedBy);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void updateInventoryFromMovementEvent(com.inventory.inventoryservice.kafka.MovementEvent event) {
        // Idempotency check
        if (event.getEventId() != null && processedEventRepository.existsById(event.getEventId())) {
            return;
        }

        // Find inventory record for source warehouse
        if (event.getSourceWarehouseId() != null) {
            Inventory sourceInventory = inventoryRepository.findByProductIdAndWarehouseId(event.getProductId(), event.getSourceWarehouseId())
                .orElse(null);
            
            if (sourceInventory != null) {
                if ("StockIssued".equals(event.getEventType()) || "StockTransferredOut".equals(event.getEventType()) || 
                    "StockReturned".equals(event.getEventType()) || "StockDamaged".equals(event.getEventType())) {
                    sourceInventory.setCurrentQuantity(sourceInventory.getCurrentQuantity() - event.getQuantity());
                    sourceInventory.setAvailableQuantity(sourceInventory.getCurrentQuantity() - sourceInventory.getReservedQuantity());
                    inventoryRepository.save(sourceInventory);
                    
                    com.inventory.inventoryservice.websocket.InventoryNotification notif = new com.inventory.inventoryservice.websocket.InventoryNotification();
                    notif.setEventType(event.getEventType());
                    notif.setInventoryId(sourceInventory.getId());
                    notif.setProductId(sourceInventory.getProductId());
                    notif.setWarehouseId(sourceInventory.getWarehouseId());
                    notif.setCurrentQuantity(sourceInventory.getCurrentQuantity());
                    notif.setAvailableQuantity(sourceInventory.getAvailableQuantity());
                    notif.setReservedQuantity(sourceInventory.getReservedQuantity());
                    notif.setTimestamp(java.time.LocalDateTime.now());
                    
                    if (webSocketPublisher != null) {
                        webSocketPublisher.sendInventoryUpdate(notif);
                    }
                }
            }
        }

        // Find inventory record for destination warehouse
        if (event.getDestinationWarehouseId() != null) {
            Inventory destInventory = inventoryRepository.findByProductIdAndWarehouseId(event.getProductId(), event.getDestinationWarehouseId())
                .orElse(null);
            
            if (destInventory != null) {
                if ("StockReceived".equals(event.getEventType()) || "StockTransferredIn".equals(event.getEventType())) {
                    destInventory.setCurrentQuantity(destInventory.getCurrentQuantity() + event.getQuantity());
                    destInventory.setAvailableQuantity(destInventory.getCurrentQuantity() - destInventory.getReservedQuantity());
                    inventoryRepository.save(destInventory);
                    
                    com.inventory.inventoryservice.websocket.InventoryNotification notif = new com.inventory.inventoryservice.websocket.InventoryNotification();
                    notif.setEventType(event.getEventType());
                    notif.setInventoryId(destInventory.getId());
                    notif.setProductId(destInventory.getProductId());
                    notif.setWarehouseId(destInventory.getWarehouseId());
                    notif.setCurrentQuantity(destInventory.getCurrentQuantity());
                    notif.setAvailableQuantity(destInventory.getAvailableQuantity());
                    notif.setReservedQuantity(destInventory.getReservedQuantity());
                    notif.setTimestamp(java.time.LocalDateTime.now());
                    
                    if (webSocketPublisher != null) {
                        webSocketPublisher.sendInventoryUpdate(notif);
                    }
                }
            }
        }

        // Save processed event for idempotency
        if (event.getEventId() != null) {
            processedEventRepository.save(new com.inventory.inventoryservice.entity.ProcessedEvent(event.getEventId(), java.time.LocalDateTime.now()));
        }
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId());
        response.setProductId(inventory.getProductId());
        response.setWarehouseId(inventory.getWarehouseId());
        response.setCurrentQuantity(inventory.getCurrentQuantity());
        response.setReservedQuantity(inventory.getReservedQuantity());
        response.setAvailableQuantity(inventory.getAvailableQuantity());
        response.setDamagedQuantity(inventory.getDamagedQuantity());
        response.setStatus(inventory.getStatus());
        response.setLastMovementAt(inventory.getLastMovementAt());
        response.setVersion(inventory.getVersion());
        return response;
    }

    @Override
    public java.util.List<TransferRecommendationResponse> getTransferRecommendations(Long productId, Integer requestedQuantity) {
        java.util.List<Inventory> inventories = inventoryRepository.findByProductIdAndStatusActive(productId);

        Integer reorderLevel = 0;
        try {
            var productResponse = productFeignClient.getProductById(productId, "Bearer " + getSystemToken());
            if (productResponse != null && productResponse.getBody() != null) {
                Object body = productResponse.getBody();
                java.lang.reflect.Field dataField = body.getClass().getDeclaredField("data");
                dataField.setAccessible(true);
                Object data = dataField.get(body);
                if (data != null) {
                    java.lang.reflect.Field rlField = data.getClass().getDeclaredField("reorderLevel");
                    rlField.setAccessible(true);
                    Object rl = rlField.get(data);
                    if (rl != null) reorderLevel = (Integer) rl;
                }
            }
        } catch (Exception e) {
            reorderLevel = 0;
        }

        final Integer finalReorderLevel = reorderLevel != null ? reorderLevel : 0;

        java.util.List<TransferRecommendationResponse> results = new java.util.ArrayList<>();

        for (Inventory inv : inventories) {
            TransferRecommendationResponse rec = new TransferRecommendationResponse();
            rec.setWarehouseId(inv.getWarehouseId());
            rec.setAvailableQuantity(inv.getAvailableQuantity());
            rec.setReservedQuantity(inv.getReservedQuantity());
            int freeStock = inv.getAvailableQuantity() - inv.getReservedQuantity();
            rec.setFreeStock(freeStock);

            try {
                var whResponse = warehouseFeignClient.getWarehouseById(inv.getWarehouseId());
                if (whResponse != null && whResponse.getBody() != null) {
                    java.lang.reflect.Field dataField = whResponse.getBody().getClass().getDeclaredField("data");
                    dataField.setAccessible(true);
                    Object data = dataField.get(whResponse.getBody());
                    if (data != null) {
                        try {
                            java.lang.reflect.Field codeField = data.getClass().getDeclaredField("warehouseCode");
                            codeField.setAccessible(true);
                            rec.setWarehouseCode((String) codeField.get(data));
                            java.lang.reflect.Field nameField = data.getClass().getDeclaredField("warehouseName");
                            nameField.setAccessible(true);
                            rec.setWarehouseName((String) nameField.get(data));
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ignored) {}

            if (freeStock >= requestedQuantity && (freeStock - requestedQuantity) >= finalReorderLevel) {
                rec.setIsRecommended(true);
                rec.setRecommendationReason("Recommended");
            } else if (freeStock >= requestedQuantity) {
                rec.setIsRecommended(false);
                rec.setRecommendationReason("Available");
            } else {
                rec.setIsRecommended(false);
                rec.setRecommendationReason("Insufficient");
            }

            results.add(rec);
        }

        results.sort((a, b) -> {
            if (a.getIsRecommended() && !b.getIsRecommended()) return -1;
            if (!a.getIsRecommended() && b.getIsRecommended()) return 1;
            return Integer.compare(b.getFreeStock(), a.getFreeStock());
        });

        return results;
    }

    private String getSystemToken() {
        return "system";
    }
}
