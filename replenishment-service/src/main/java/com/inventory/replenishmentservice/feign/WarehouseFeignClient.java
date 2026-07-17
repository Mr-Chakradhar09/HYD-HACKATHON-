package com.inventory.replenishmentservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "warehouse-service", url = "${feign.clients.warehouse-service.url:http://localhost:8083}")
public interface WarehouseFeignClient {
    @GetMapping("/internal/warehouses/{warehouseId}")
    ResponseEntity<Object> getWarehouseById(@PathVariable Long warehouseId);
}
