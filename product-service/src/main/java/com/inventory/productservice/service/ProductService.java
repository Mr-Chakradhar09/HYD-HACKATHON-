package com.inventory.productservice.service;

import com.inventory.productservice.dto.request.CreateProductRequest;
import com.inventory.productservice.dto.request.UpdateProductRequest;
import com.inventory.productservice.dto.response.ProductResponse;
import com.inventory.productservice.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request, String createdBy);

    ProductResponse getProductById(Long id);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    ProductResponse updateProduct(Long id, UpdateProductRequest request, String updatedBy);

    ProductResponse toggleProductStatus(Long id, String updatedBy);

    Page<ProductResponse> searchProducts(
            String search, Long categoryId, String brand,
            ProductStatus status, String unit,
            java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
            Pageable pageable);
}
