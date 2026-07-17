package com.inventory.productservice.controller;

import com.inventory.productservice.dto.request.CreateProductRequest;
import com.inventory.productservice.dto.request.UpdateProductRequest;
import com.inventory.productservice.dto.response.ApiResponse;
import com.inventory.productservice.dto.response.ProductResponse;
import com.inventory.productservice.enums.ProductStatus;
import com.inventory.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication) {
        ProductResponse product = productService.createProduct(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Product created successfully", product, java.time.LocalDateTime.now(), "", HttpStatus.CREATED.value()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product fetched successfully", product, java.time.LocalDateTime.now(), "", HttpStatus.OK.value()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        if (size <= 0) size = 20;
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Products fetched successfully", products, java.time.LocalDateTime.now(), "", HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request,
            Authentication authentication) {
        ProductResponse product = productService.updateProduct(id, request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Product updated successfully", product, java.time.LocalDateTime.now(), "", HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleProductStatus(
            @PathVariable Long id,
            Authentication authentication) {
        ProductResponse product = productService.toggleProductStatus(id, authentication.getName());
        String message = product.getStatus() == ProductStatus.ACTIVE ? "Product activated successfully" : "Product deactivated successfully";
        return ResponseEntity.ok(new ApiResponse<>(true, message, product, java.time.LocalDateTime.now(), "", HttpStatus.OK.value()));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> products = productService.searchProducts(
                search, categoryId, brand, status, unit, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Products found", products, java.time.LocalDateTime.now(), "", HttpStatus.OK.value()));
    }
}
