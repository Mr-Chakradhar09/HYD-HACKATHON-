package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.request.CreateInventoryRequest;
import com.inventory.inventoryservice.dto.response.InventoryResponse;
import com.inventory.inventoryservice.dto.response.TransferRecommendationResponse;
import com.inventory.inventoryservice.enums.InventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {
    InventoryResponse createInventory(CreateInventoryRequest request, String createdBy);
    InventoryResponse getInventoryById(Long id);
    Page<InventoryResponse> getAllInventories(Pageable pageable);
    Page<InventoryResponse> searchInventories(String search, Long warehouseId, Long productId, InventoryStatus status, Pageable pageable);
    void deactivateInventory(Long id, String updatedBy);
    void updateInventoryFromMovementEvent(com.inventory.inventoryservice.kafka.MovementEvent event);
    List<TransferRecommendationResponse> getTransferRecommendations(Long productId, Integer requestedQuantity);
}
