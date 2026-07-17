package com.inventory.product;

import com.inventory.product.controller.ProductController;
import com.inventory.product.dto.ProductRequest;
import com.inventory.product.dto.ProductResponse;
import com.inventory.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void createProduct_ShouldReturnCreated() {
        when(productService.createProduct(any())).thenReturn(new ProductResponse());
        assertEquals(HttpStatus.CREATED, productController.createProduct(new ProductRequest()).getStatusCode());
    }

    @Test
    void getAllProducts_ShouldReturnList() {
        when(productService.getAllProducts()).thenReturn(List.of(new ProductResponse()));
        assertEquals(1, productController.getAllProducts(null).getBody().size());
    }

    @Test
    void getProductById_ShouldSucceed() {
        when(productService.getProductById(1L)).thenReturn(new ProductResponse());
        assertTrue(productController.getProductById(1L).getStatusCode().is2xxSuccessful());
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() {
        doNothing().when(productService).deleteProduct(1L);
        assertEquals(HttpStatus.NO_CONTENT, productController.deleteProduct(1L).getStatusCode());
    }
}
