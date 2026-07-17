package com.inventory.warehouseservice.service.interfaces;

import com.inventory.warehouseservice.dto.request.CreateWarehouseRequest;
import com.inventory.warehouseservice.dto.request.UpdateWarehouseRequest;
import com.inventory.warehouseservice.dto.response.WarehouseResponse;
import com.inventory.warehouseservice.dto.response.WarehouseSummaryResponse;
import com.inventory.warehouseservice.enums.WarehouseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {

    WarehouseResponse createWarehouse(CreateWarehouseRequest request);
    WarehouseResponse updateWarehouse(Long warehouseId, UpdateWarehouseRequest request);
    WarehouseResponse getWarehouseById(Long warehouseId);
    WarehouseResponse getWarehouseByCode(String warehouseCode);
    Page<WarehouseSummaryResponse> getAllWarehouses(Pageable pageable);
    Page<WarehouseSummaryResponse> searchWarehouses(String keyword, WarehouseStatus status, Pageable pageable);
    void activateWarehouse(Long warehouseId);
    void deactivateWarehouse(Long warehouseId);
    void deleteWarehouse(Long warehouseId);

}
