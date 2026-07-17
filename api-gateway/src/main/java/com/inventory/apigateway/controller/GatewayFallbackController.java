package com.inventory.apigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    private static final Logger log = LoggerFactory.getLogger(GatewayFallbackController.class);

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        log.warn("Circuit breaker triggered for auth-service");
        return buildResponse("Auth Service is currently unavailable. Please try again later.", "/api/v1/auth");
    }

    @RequestMapping("/product")
    public ResponseEntity<Map<String, Object>> productFallback() {
        log.warn("Circuit breaker triggered for product-service");
        return buildResponse("Product Service is currently unavailable. Please try again later.", "/api/v1/products");
    }

    @RequestMapping("/warehouse")
    public ResponseEntity<Map<String, Object>> warehouseFallback() {
        log.warn("Circuit breaker triggered for warehouse-service");
        return buildResponse("Warehouse Service is currently unavailable. Please try again later.", "/api/v1/warehouses");
    }

    @RequestMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventoryFallback() {
        log.warn("Circuit breaker triggered for inventory-service");
        return buildResponse("Inventory Service is currently unavailable. Please try again later.", "/api/v1/inventory");
    }

    @RequestMapping("/movement")
    public ResponseEntity<Map<String, Object>> movementFallback() {
        log.warn("Circuit breaker triggered for movement-service");
        return buildResponse("Movement Service is currently unavailable. Please try again later.", "/api/v1/movements");
    }

    @RequestMapping("/replenishment")
    public ResponseEntity<Map<String, Object>> replenishmentFallback() {
        log.warn("Circuit breaker triggered for replenishment-service");
        return buildResponse("Replenishment Service is currently unavailable. Please try again later.", "/api/v1/replenishment");
    }

    @RequestMapping("/reporting")
    public ResponseEntity<Map<String, Object>> reportingFallback() {
        log.warn("Circuit breaker triggered for reporting-service");
        return buildResponse("Reporting Service is currently unavailable. Please try again later.", "/api/v1/reports");
    }

    @RequestMapping
    public ResponseEntity<Map<String, Object>> genericFallback() {
        log.warn("Circuit breaker triggered (generic)");
        return buildResponse("Service is currently unavailable. Please try again later.", "/");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String message, String path) {
        Map<String, Object> body = Map.of(
            "success", false,
            "message", message,
            "path", path,
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
