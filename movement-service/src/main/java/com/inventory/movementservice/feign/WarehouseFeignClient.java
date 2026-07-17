package com.inventory.movementservice.feign;

import com.inventory.movementservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "warehouse-service", url = "${feign.clients.warehouse-service.url:http://localhost:8083}")
public interface WarehouseFeignClient {

    @GetMapping("/internal/warehouses/{warehouseId}")
    ApiResponse<Object> getWarehouseById(@PathVariable Long warehouseId);

    @GetMapping("/internal/warehouses/{warehouseId}/assignments")
    ApiResponse<List<Map<String, Object>>> getAssignmentsByWarehouse(@PathVariable Long warehouseId);
}
