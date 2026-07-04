package com.inventory.report.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;

@Service
public class ReportService {

    private final RestClient restClient;

    public ReportService(@Value("${inventory.service.url}") String inventoryUrl) {
        this.restClient = RestClient.create(inventoryUrl);
    }

    public List<Map<String, Object>> getInventoryReport() {
        List<Map<String, Object>> inventory = restClient.get()
                .uri("/api/v1/inventory")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return inventory != null ? inventory : List.of();
    }

    public List<Map<String, Object>> getMovementReport() {

        return List.of(
                Map.of("type", "INBOUND", "count", 150, "totalQuantity", 5000),
                Map.of("type", "OUTBOUND", "count", 120, "totalQuantity", 3500),
                Map.of("type", "TRANSFER", "count", 45, "totalQuantity", 1200),
                Map.of("type", "ADJUSTMENT", "count", 30, "totalQuantity", 800)
        );
    }

    public List<Map<String, Object>> getWarehouseReport() {
        List<Map<String, Object>> inventory = getInventoryReport();
        Map<Long, Map<String, Object>> warehouseMap = new HashMap<>();

        for (Map<String, Object> item : inventory) {
            Long whId = ((Number) item.get("warehouseId")).longValue();
            warehouseMap.putIfAbsent(whId, new HashMap<>(Map.of(
                    "warehouseId", whId,
                    "totalProducts", 0,
                    "totalQuantity", 0
            )));
            Map<String, Object> wh = warehouseMap.get(whId);
            wh.put("totalProducts", ((Number) wh.get("totalProducts")).intValue() + 1);
            wh.put("totalQuantity", ((Number) wh.get("totalQuantity")).intValue() + ((Number) item.get("quantity")).intValue());
        }

        return new ArrayList<>(warehouseMap.values());
    }
}
