package com.inventory.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.productservice.dto.request.CreateProductRequest;
import com.inventory.productservice.dto.response.ProductResponse;
import com.inventory.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse testResponse;

    @BeforeEach
    void setUp() {
        testResponse = new ProductResponse();
        testResponse.setId(1L);
        testResponse.setProductCode("PROD-001");
        testResponse.setName("Test Product");
        testResponse.setBasePrice(new BigDecimal("99.99"));
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testResponse);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productCode").value("PROD-001"))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    void testCreateProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductCode("PROD-001");
        request.setName("Test Product");
        request.setCategory("Electronics");
        request.setBasePrice(new BigDecimal("99.99"));

        when(productService.createProduct(any(CreateProductRequest.class), anyString())).thenReturn(testResponse);

        mockMvc.perform(post("/api/v1/products")
                .header("X-User-Id", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productCode").value("PROD-001"));
    }
}
