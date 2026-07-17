package com.inventory.reportingservice.service.interfaces;

import com.inventory.reportingservice.dto.response.MovementHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportingService {
    Page<MovementHistoryResponse> getMovementsByProductId(Long productId, Pageable pageable);
    Page<MovementHistoryResponse> getMovementsByWarehouseId(Long warehouseId, Pageable pageable);
    Page<MovementHistoryResponse> getAllMovements(Pageable pageable);
    Object getInventorySummary(Long warehouseId, Long productId, int page, int size);
}
