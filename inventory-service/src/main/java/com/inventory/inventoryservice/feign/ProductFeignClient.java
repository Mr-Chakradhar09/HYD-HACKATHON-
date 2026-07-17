package com.inventory.inventoryservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "product-service", url = "${feign.clients.product-service.url:http://localhost:8082}")
public interface ProductFeignClient {
    @GetMapping("/api/v1/products/{productId}")
    ResponseEntity<Object> getProductById(@PathVariable Long productId, @RequestHeader("Authorization") String authHeader);
}
