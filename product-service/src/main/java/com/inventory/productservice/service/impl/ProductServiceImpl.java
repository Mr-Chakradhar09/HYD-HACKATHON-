package com.inventory.productservice.service.impl;

import com.inventory.productservice.dto.request.CreateProductRequest;
import com.inventory.productservice.dto.request.UpdateProductRequest;
import com.inventory.productservice.dto.response.ProductResponse;
import com.inventory.productservice.entity.Category;
import com.inventory.productservice.entity.Product;
import com.inventory.productservice.enums.CategoryStatus;
import com.inventory.productservice.enums.ProductStatus;
import com.inventory.productservice.exception.DuplicateResourceException;
import com.inventory.productservice.exception.IncompatibleOptimisticLockException;
import com.inventory.productservice.exception.InvalidOperationException;
import com.inventory.productservice.exception.ResourceNotFoundException;
import com.inventory.productservice.repository.CategoryRepository;
import com.inventory.productservice.repository.ProductRepository;
import com.inventory.productservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, String createdBy) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.getSku());
        }
        if (request.getBarcode() != null && !request.getBarcode().isEmpty()) {
            if (productRepository.existsByBarcode(request.getBarcode())) {
                throw new DuplicateResourceException("Barcode already exists: " + request.getBarcode());
            }
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        if (category.getStatus() != CategoryStatus.ACTIVE) {
            throw new InvalidOperationException("Category is not active");
        }
        Product product = new Product();
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setCategory(category);
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setReorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 0);
        product.setCreatedBy(createdBy);
        productRepository.save(product);
        return mapToResponse(product);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(Long id, UpdateProductRequest request, String updatedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (!product.getVersion().equals(request.getVersion())) {
            throw new IncompatibleOptimisticLockException("Product has been modified by another user. Please refresh and try again.");
        }
        if (productRepository.existsBySkuAndIdNot(product.getSku(), id)) {
            throw new DuplicateResourceException("SKU already exists");
        }
        if (request.getBarcode() != null && !request.getBarcode().isEmpty()) {
            if (productRepository.existsByBarcodeAndIdNot(request.getBarcode(), id)) {
                throw new DuplicateResourceException("Barcode already exists: " + request.getBarcode());
            }
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setCategory(category);
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setReorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : product.getReorderLevel());
        product.setUpdatedBy(updatedBy);
        productRepository.save(product);
        return mapToResponse(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse toggleProductStatus(Long id, String updatedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (product.getStatus() == ProductStatus.INACTIVE) {
            product.setStatus(ProductStatus.ACTIVE);
        } else {
            product.setStatus(ProductStatus.INACTIVE);
        }
        product.setUpdatedBy(updatedBy);
        productRepository.save(product);
        return mapToResponse(product);
    }

    @Override
    public Page<ProductResponse> searchProducts(
            String search, Long categoryId, String brand,
            ProductStatus status, String unit,
            java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
            Pageable pageable) {
        com.inventory.productservice.enums.UnitType unitType = null;
        if (unit != null && !unit.isEmpty()) {
            try {
                unitType = com.inventory.productservice.enums.UnitType.valueOf(unit.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return productRepository.searchProducts(
                search, categoryId, brand, status, unitType, minPrice, maxPrice, pageable
        ).map(this::mapToResponse);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSku(product.getSku());
        response.setBarcode(product.getBarcode());
        response.setProductName(product.getProductName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setUnit(product.getUnit());
        response.setPrice(product.getPrice());
        response.setStatus(product.getStatus());
        response.setImageUrl(product.getImageUrl());
        response.setVersion(product.getVersion());
        response.setCreatedBy(product.getCreatedBy());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedBy(product.getUpdatedBy());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setReorderLevel(product.getReorderLevel());
        return response;
    }
}
