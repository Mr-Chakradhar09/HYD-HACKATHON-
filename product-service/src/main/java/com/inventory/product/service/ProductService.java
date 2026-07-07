package com.inventory.product.service;

import com.inventory.product.dto.ProductRequest;
import com.inventory.product.dto.ProductResponse;
import com.inventory.product.entity.Product;
import com.inventory.product.enums.ProductStatus;
import com.inventory.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setUnitPrice(request.getUnitPrice());
        product.setUnitOfMeasure(request.getUnitOfMeasure() != null ? request.getUnitOfMeasure() : "PCS");
        product.setMinimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : 0);
        product = productRepository.save(product);
        return ProductResponse.fromEntity(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(ProductResponse::fromEntity).toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductResponse.fromEntity(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setUnitPrice(request.getUnitPrice());
        product.setUnitOfMeasure(request.getUnitOfMeasure() != null ? request.getUnitOfMeasure() : product.getUnitOfMeasure());
        product.setMinimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : 0);
        product = productRepository.save(product);
        return ProductResponse.fromEntity(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    public List<ProductResponse> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream().map(ProductResponse::fromEntity).toList();
    }
}
