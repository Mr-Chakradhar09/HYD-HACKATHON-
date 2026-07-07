package com.inventory.product;

import com.inventory.product.dto.ProductRequest;
import com.inventory.product.dto.ProductResponse;
import com.inventory.product.entity.Product;
import com.inventory.product.enums.Category;
import com.inventory.product.enums.ProductStatus;
import com.inventory.product.repository.ProductRepository;
import com.inventory.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_ShouldSucceed() {
        ProductRequest req = new ProductRequest();
        req.setSku("SKU001");
        req.setName("Test Product");
        req.setCategory(Category.ELECTRONICS);
        req.setUnitPrice(new BigDecimal("99.99"));
        req.setUnitOfMeasure("M3");
        req.setMinimumStock(10);

        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse res = productService.createProduct(req);
        assertEquals("SKU001", res.getSku());
        assertEquals("Test Product", res.getName());
        assertEquals("M3", res.getUnitOfMeasure());
        assertEquals(Category.ELECTRONICS, res.getCategory());
    }

    @Test
    void createProduct_DefaultUom_ShouldBePCS() {
        ProductRequest req = new ProductRequest();
        req.setSku("SKU002");
        req.setName("No UOM");
        req.setUnitOfMeasure(null);

        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(2L);
            return p;
        });

        ProductResponse res = productService.createProduct(req);
        assertEquals("PCS", res.getUnitOfMeasure());
    }

    @Test
    void getAllProducts_ShouldReturnList() {
        Product p1 = new Product(); p1.setSku("SKU001"); p1.setName("P1");
        Product p2 = new Product(); p2.setSku("SKU002"); p2.setName("P2");

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        assertEquals(2, productService.getAllProducts().size());
    }

    @Test
    void getProductById_ShouldReturn() {
        Product p = new Product(); p.setId(1L); p.setSku("SKU001"); p.setName("Test");

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        ProductResponse res = productService.getProductById(1L);
        assertEquals("SKU001", res.getSku());
    }

    @Test
    void getProductById_NotFound_ShouldThrow() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.getProductById(99L));
    }

    @Test
    void updateProduct_ShouldUpdate() {
        Product p = new Product(); p.setId(1L); p.setSku("SKU001"); p.setName("Old");

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductRequest req = new ProductRequest();
        req.setSku("SKU001-NEW");
        req.setName("New Name");
        req.setUnitOfMeasure("KG");

        ProductResponse res = productService.updateProduct(1L, req);
        assertEquals("SKU001-NEW", res.getSku());
        assertEquals("New Name", res.getName());
        assertEquals("KG", res.getUnitOfMeasure());
    }

    @Test
    void deleteProduct_ShouldSetInactive() {
        Product p = new Product(); p.setId(1L); p.setStatus(ProductStatus.ACTIVE);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        productService.deleteProduct(1L);
        assertEquals(ProductStatus.INACTIVE, p.getStatus());
    }

    @Test
    void searchProducts_ShouldReturnMatches() {
        Product p = new Product(); p.setName("Widget");

        when(productRepository.findByNameContainingIgnoreCase("Widget")).thenReturn(List.of(p));

        List<ProductResponse> results = productService.searchProducts("Widget");
        assertEquals(1, results.size());
    }
}
