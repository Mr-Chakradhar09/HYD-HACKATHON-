package com.inventory.warehouseservice.service.interfaces;

import com.inventory.warehouseservice.entity.Warehouse;

import java.util.List;

public interface WarehouseInternalService {
    Warehouse getWarehouseEntity(Long warehouseId);
    boolean isWarehouseActive(Long warehouseId);
    List<Long> getAssignedEmployees(Long warehouseId);

}