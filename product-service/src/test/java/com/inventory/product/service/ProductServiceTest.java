package com.inventory.product.service;

import com.inventory.product.dto.ProductRequest;
import com.inventory.product.dto.ProductResponse;
import com.inventory.product.entity.Product;
import com.inventory.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void testCreateProduct() {
        ProductRequest req = new ProductRequest();
        req.setName("Test Laptop");
        req.setSku("LAP-001");
        req.setUnitPrice(BigDecimal.valueOf(1000.0));

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Test Laptop");
        saved.setSku("LAP-001");
        saved.setUnitPrice(BigDecimal.valueOf(1000.0));

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(req);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("LAP-001", response.getSku());
    }

    @Test
    void testGetProduct() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Test Laptop");

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        ProductResponse response = productService.getProductById(1L);

        assertEquals("Test Laptop", response.getName());
        verify(productRepository).findById(1L);
    }
}
