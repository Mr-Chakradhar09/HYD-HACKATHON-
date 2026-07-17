package com.inventory.productservice.service;

import com.inventory.productservice.dto.request.CreateProductRequest;
import com.inventory.productservice.dto.response.ProductResponse;
import com.inventory.productservice.entity.Product;
import com.inventory.productservice.enums.ProductStatus;
import com.inventory.productservice.repository.ProductRepository;
import com.inventory.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setProductCode("PROD-001");
        testProduct.setName("Test Product");
        testProduct.setCategory("Electronics");
        testProduct.setBasePrice(new BigDecimal("99.99"));
        testProduct.setStatus(ProductStatus.ACTIVE);
    }

    @Test
    void testCreateProduct() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductCode("PROD-001");
        request.setName("Test Product");
        request.setCategory("Electronics");
        request.setBasePrice(new BigDecimal("99.99"));

        when(productRepository.existsByProductCode("PROD-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.createProduct(request, "admin");

        assertNotNull(response);
        assertEquals("PROD-001", response.getProductCode());
        assertEquals("Test Product", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PROD-001", response.getProductCode());
    }
}
