package com.inventory.productservice.controller;

import com.inventory.productservice.dto.response.ApiResponse;
import com.inventory.productservice.dto.response.ProductResponse;
import com.inventory.productservice.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {

    private final ProductService productService;

    public InternalProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long productId,
            HttpServletRequest request) {
            
        ProductResponse response = productService.getProductById(productId);
        
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                true,
                "Product retrieved successfully.",
                response,
                LocalDateTime.now(),
                request.getRequestURI(),
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(apiResponse);
    }
}
