package com.inventory.reportingservice.service.impl;

import com.inventory.reportingservice.dto.response.MovementHistoryResponse;
import com.inventory.reportingservice.entity.MovementHistory;
import com.inventory.reportingservice.feign.InventoryFeignClient;
import com.inventory.reportingservice.repository.MovementHistoryRepository;
import com.inventory.reportingservice.service.interfaces.ReportingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ReportingServiceImpl implements ReportingService {

    private final MovementHistoryRepository repository;
    private final InventoryFeignClient inventoryFeignClient;

    public ReportingServiceImpl(MovementHistoryRepository repository, InventoryFeignClient inventoryFeignClient) {
        this.repository = repository;
        this.inventoryFeignClient = inventoryFeignClient;
    }

    @Override
    public Page<MovementHistoryResponse> getMovementsByProductId(Long productId, Pageable pageable) {
        return repository.findByProductId(productId, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<MovementHistoryResponse> getMovementsByWarehouseId(Long warehouseId, Pageable pageable) {
        return repository.findBySourceWarehouseIdOrDestinationWarehouseId(warehouseId, warehouseId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<MovementHistoryResponse> getAllMovements(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Object getInventorySummary(Long warehouseId, Long productId, int page, int size) {
        ResponseEntity<Object> response = inventoryFeignClient.searchInventories(null, warehouseId, productId, null, page, size);
        return response.getBody();
    }

    private MovementHistoryResponse mapToResponse(MovementHistory entity) {
        MovementHistoryResponse response = new MovementHistoryResponse();
        response.setId(entity.getId());
        response.setEventId(entity.getEventId());
        response.setMovementNumber(entity.getMovementNumber());
        response.setMovementType(entity.getMovementType());
        response.setProductId(entity.getProductId());
        response.setSourceWarehouseId(entity.getSourceWarehouseId());
        response.setDestinationWarehouseId(entity.getDestinationWarehouseId());
        response.setQuantity(entity.getQuantity());
        response.setPerformedBy(entity.getPerformedBy());
        response.setMovementTimestamp(entity.getMovementTimestamp());
        return response;
    }
}
