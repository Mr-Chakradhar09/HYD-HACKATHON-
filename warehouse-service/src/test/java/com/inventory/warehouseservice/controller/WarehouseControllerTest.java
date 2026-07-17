package com.inventory.warehouseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.warehouseservice.dto.request.CreateWarehouseRequest;
import com.inventory.warehouseservice.dto.response.WarehouseResponse;
import com.inventory.warehouseservice.enums.WarehouseType;
import com.inventory.warehouseservice.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseController.class)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WarehouseService warehouseService;

    @Autowired
    private ObjectMapper objectMapper;

    private WarehouseResponse testResponse;

    @BeforeEach
    void setUp() {
        testResponse = new WarehouseResponse();
        testResponse.setId(1L);
        testResponse.setWarehouseCode("WH-001");
        testResponse.setWarehouseName("Main Hub");
        testResponse.setType(WarehouseType.MAIN);
    }

    @Test
    void testGetWarehouseById() throws Exception {
        when(warehouseService.getWarehouseById(1L)).thenReturn(testResponse);

        mockMvc.perform(get("/api/v1/warehouses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.warehouseCode").value("WH-001"))
                .andExpect(jsonPath("$.data.warehouseName").value("Main Hub"));
    }

    @Test
    void testCreateWarehouse() throws Exception {
        CreateWarehouseRequest request = new CreateWarehouseRequest();
        request.setWarehouseCode("WH-001");
        request.setWarehouseName("Main Hub");
        request.setType(WarehouseType.MAIN);

        when(warehouseService.createWarehouse(any(CreateWarehouseRequest.class), anyString())).thenReturn(testResponse);

        mockMvc.perform(post("/api/v1/warehouses")
                .header("X-User-Id", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.warehouseCode").value("WH-001"));
    }
}
